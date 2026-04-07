package com.coursecloud.service;

import com.coursecloud.dto.AuthResponse;
import com.coursecloud.dto.LoginRequest;
import com.coursecloud.dto.RegisterRequest;
import com.coursecloud.dto.UserDTO;
import com.coursecloud.entity.User;
import com.coursecloud.repository.UserRepository;
import com.coursecloud.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.verification.token.expiry.hours:24}")
    private int tokenExpiryHours;

    @Value("${app.otp.expiry.minutes:10}")
    private int otpExpiryMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // ── STEP 1: Register → creates PENDING user, sends OTP ───────────────────

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        String email = request.getEmail().toLowerCase().trim();

        // If user already exists and is ACTIVE → error
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (existing.getStatus() == User.Status.ACTIVE) {
                throw new IllegalArgumentException("An account with this email already exists. Please sign in.");
            }
            // If PENDING (previous incomplete registration) → delete and re-register
            userRepository.delete(existing);
            userRepository.flush();
        });

        // Parse role
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        String name = request.getName().trim();
        String initials = buildInitials(name);

        // Generate 6-digit OTP
        String otp = generateOtp();

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .initials(initials)
                .emailVerified(false)
                .status(User.Status.PENDING)  // PENDING until OTP verified
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verificationToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);
        log.info("User created in PENDING state: {} ({})", email, role);

        // Send OTP email
        try {
            emailService.sendOtpEmail(email, name, otp);
            log.info("OTP email sent to {}", email);
        } catch (Exception ex) {
            log.warn("OTP email sending failed: {}", ex.getMessage());
            // Don't block — user can use resend-otp
        }

        return AuthResponse.builder()
                .message("OTP sent to " + email + ". Please enter the 6-digit code to verify your email.")
                .build();
    }

    // ── STEP 2: Verify OTP → activates user, returns JWT ─────────────────────

    public AuthResponse verifyOtp(String email, String otp) {
        log.info("OTP verification attempt for: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email."));

        if (user.getStatus() == User.Status.ACTIVE) {
            throw new IllegalArgumentException("This account is already verified. Please sign in.");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid OTP. Please check your email and try again.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        // Activate user
        user.setEmailVerified(true);
        user.setStatus(User.Status.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        log.info("User verified and activated: {}", email);

        // Send welcome email (non-blocking)
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception ex) {
            log.warn("Welcome email failed: {}", ex.getMessage());
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(jwt)
                .user(UserDTO.fromEntity(user))
                .message("Email verified! Welcome to Course Cloud.")
                .build();
    }

    // ── Resend OTP ────────────────────────────────────────────────────────────

    public AuthResponse resendOtp(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email."));

        if (user.getStatus() == User.Status.ACTIVE) {
            throw new IllegalArgumentException("Account is already verified.");
        }

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp);
        log.info("OTP resent to {}", email);

        return AuthResponse.builder()
                .message("New OTP sent to " + email)
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with that email address."));

        if (user.getStatus() == User.Status.PENDING) {
            throw new IllegalArgumentException("Please verify your email first. Check your inbox for the OTP.");
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new IllegalArgumentException("Your account is deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        log.info("Login successful: {}", user.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .user(UserDTO.fromEntity(user))
                .message("Login successful!")
                .build();
    }

    // ── Legacy link-based verify email (kept for backward compatibility) ───────

    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token."));

        if (user.isEmailVerified()) return "Email is already verified.";

        if (user.getVerificationTokenExpiry() != null &&
                user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        user.setEmailVerified(true);
        user.setStatus(User.Status.ACTIVE);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        try { emailService.sendWelcomeEmail(user.getEmail(), user.getName()); } catch (Exception ignored) {}
        log.info("Email verified (link) for: {}", user.getEmail());
        return "Email verified successfully! You can now sign in.";
    }

    public String resendVerification(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with that email."));
        if (user.isEmailVerified()) return "Email is already verified.";

        // For OTP-based pending users, resend OTP
        if (user.getStatus() == User.Status.PENDING) {
            String otp = generateOtp();
            user.setOtpCode(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            userRepository.save(user);
            emailService.sendOtpEmail(user.getEmail(), user.getName(), otp);
            return "New OTP sent to " + email;
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(tokenExpiryHours));
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);
        return "Verification email resent. Please check your inbox.";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private String buildInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.length() > 2 ? sb.substring(0, 2) : sb.toString();
    }
}
