package com.coursecloud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String initials;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;   // PENDING until OTP verified

    // ── Email verification ────────────────────────────────────────────────────

    @Builder.Default
    private boolean emailVerified = false;

    // Legacy link-based verification token (kept for backward compatibility)
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    // ── OTP-based verification (used during registration) ─────────────────────

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    // ── Metadata ──────────────────────────────────────────────────────────────

    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "courses_count")
    @Builder.Default
    private int courses = 0;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum Role {
        Admin, Instructor, Student, CONTENT_CREATOR
    }

    public enum Status {
        ACTIVE, INACTIVE, PENDING   // PENDING = registered but OTP not verified
    }
}
