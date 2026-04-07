package com.coursecloud.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Verification Email ─────────────────────────────────────────────────────

    public void sendVerificationEmail(String toEmail, String userName, String token) {
        // Link opens the FRONTEND page which calls the backend API
        String verifyUrl = frontendUrl + "/?token=" + token;
        String subject   = "✅ Verify your Course Cloud account";
        String html      = buildVerificationHtml(userName, verifyUrl);
        sendHtmlEmail(toEmail, subject, html);
        log.info("Verification email sent → {}", toEmail);
    }

    // ── Welcome Email ──────────────────────────────────────────────────────────

    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "🎉 Welcome to Course Cloud, " + userName + "!";
        String html    = buildWelcomeHtml(userName);
        sendHtmlEmail(toEmail, subject, html);
        log.info("Welcome email sent → {}", toEmail);
    }

    // ── OTP Email ──────────────────────────────────────────────────────────────

    public void sendOtpEmail(String toEmail, String userName, String otp) {
        String subject = "🔐 Your Course Cloud verification code: " + otp;
        String html    = buildOtpHtml(userName, otp);
        sendHtmlEmail(toEmail, subject, html);
        log.info("OTP email sent → {}", toEmail);
    }

    // ── Internal send helper ───────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("Course Cloud <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    // ── HTML Templates ─────────────────────────────────────────────────────────

    private String buildVerificationHtml(String userName, String verifyUrl) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Verify your email – Course Cloud</title>
            </head>
            <body style="margin:0;padding:0;background:#0f0f1a;font-family:'Segoe UI',Roboto,Arial,sans-serif;">

              <!-- Wrapper -->
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f0f1a;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background:#1a1a2e;border-radius:16px;overflow:hidden;
                                  border:1px solid rgba(108,99,255,0.25);
                                  box-shadow:0 20px 60px rgba(0,0,0,0.5);">

                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#6c63ff,#4facfe);padding:40px 48px;text-align:center;">
                          <div style="display:inline-flex;align-items:center;gap:12px;">
                            <div style="background:rgba(255,255,255,0.2);border-radius:12px;
                                        width:48px;height:48px;display:inline-flex;
                                        align-items:center;justify-content:center;
                                        font-size:24px;line-height:48px;text-align:center;">📚</div>
                          </div>
                          <div style="color:#fff;font-size:26px;font-weight:800;margin-top:12px;
                                      letter-spacing:-0.5px;">Course Cloud</div>
                          <div style="color:rgba(255,255,255,0.75);font-size:13px;
                                      letter-spacing:2px;text-transform:uppercase;margin-top:4px;">
                            Learning Platform
                          </div>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:48px;">

                          <!-- Icon -->
                          <div style="text-align:center;margin-bottom:28px;">
                            <div style="display:inline-block;background:rgba(108,99,255,0.15);
                                        border:2px solid rgba(108,99,255,0.4);
                                        border-radius:50%%;width:80px;height:80px;
                                        line-height:80px;font-size:36px;text-align:center;">
                              ✉️
                            </div>
                          </div>

                          <!-- Greeting -->
                          <h1 style="color:#fff;font-size:24px;font-weight:700;
                                     text-align:center;margin:0 0 12px;">
                            Verify your email address
                          </h1>
                          <p style="color:#a0a0c0;font-size:15px;line-height:1.7;
                                    text-align:center;margin:0 0 36px;">
                            Hi <strong style="color:#c4b5fd;">%s</strong>! 👋<br/>
                            Thanks for joining Course Cloud. Click the button below to
                            verify your email address and activate your account.
                          </p>

                          <!-- CTA Button -->
                          <div style="text-align:center;margin-bottom:36px;">
                            <a href="%s"
                               style="display:inline-block;
                                      background:linear-gradient(135deg,#6c63ff,#4facfe);
                                      color:#fff;font-size:16px;font-weight:700;
                                      text-decoration:none;padding:16px 48px;
                                      border-radius:50px;
                                      box-shadow:0 8px 32px rgba(108,99,255,0.4);">
                              ✅ &nbsp; Verify My Email
                            </a>
                          </div>

                          <!-- Fallback link -->
                          <div style="background:rgba(255,255,255,0.04);border-radius:10px;
                                      padding:16px 20px;margin-bottom:36px;">
                            <p style="color:#7070a0;font-size:12px;margin:0 0 8px;">
                              Or copy and paste this link into your browser:
                            </p>
                            <p style="color:#6c63ff;font-size:12px;word-break:break-all;margin:0;">
                              %s
                            </p>
                          </div>

                          <!-- Warning -->
                          <div style="border-left:3px solid #fbbf24;padding-left:16px;
                                      margin-bottom:36px;">
                            <p style="color:#fbbf24;font-size:13px;margin:0;font-weight:600;">
                              ⏰ This link expires in 24 hours
                            </p>
                            <p style="color:#7070a0;font-size:12px;margin:4px 0 0;">
                              If you did not create a Course Cloud account, you can safely ignore this email.
                            </p>
                          </div>

                          <!-- Divider -->
                          <hr style="border:none;border-top:1px solid rgba(255,255,255,0.08);margin:0 0 24px;"/>

                          <!-- Footer note -->
                          <p style="color:#7070a0;font-size:12px;text-align:center;margin:0;">
                            Need help? Reply to this email or contact
                            <a href="mailto:harshavardhan2286@gmail.com"
                               style="color:#6c63ff;text-decoration:none;">
                              harshavardhan2286@gmail.com
                            </a>
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:rgba(0,0,0,0.2);padding:20px 48px;text-align:center;">
                          <p style="color:#5050a0;font-size:11px;margin:0;">
                            © 2025 Course Cloud · All rights reserved · You're receiving this because you signed up
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>

            </body>
            </html>
            """.formatted(userName, verifyUrl, verifyUrl);
    }

    private String buildWelcomeHtml(String userName) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <title>Welcome to Course Cloud!</title>
            </head>
            <body style="margin:0;padding:0;background:#0f0f1a;font-family:'Segoe UI',Roboto,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f0f1a;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background:#1a1a2e;border-radius:16px;overflow:hidden;
                                  border:1px solid rgba(52,211,153,0.25);
                                  box-shadow:0 20px 60px rgba(0,0,0,0.5);">

                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#10b981,#06b6d4);
                                   padding:40px 48px;text-align:center;">
                          <div style="font-size:48px;margin-bottom:8px;">🎉</div>
                          <div style="color:#fff;font-size:26px;font-weight:800;">Welcome to Course Cloud!</div>
                          <div style="color:rgba(255,255,255,0.75);font-size:13px;
                                      letter-spacing:2px;text-transform:uppercase;margin-top:4px;">
                            Your account is now verified
                          </div>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:48px;">
                          <p style="color:#a0a0c0;font-size:16px;line-height:1.7;text-align:center;margin:0 0 32px;">
                            Hi <strong style="color:#34d399;">%s</strong>! 🌟<br/><br/>
                            Your email has been verified. You're all set to start your
                            learning journey on <strong style="color:#fff;">Course Cloud</strong>!
                          </p>

                          <!-- Features grid -->
                          <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:36px;">
                            <tr>
                              <td width="50%%" style="padding:0 8px 16px 0;">
                                <div style="background:rgba(255,255,255,0.04);border-radius:12px;padding:20px;
                                            border:1px solid rgba(108,99,255,0.2);">
                                  <div style="font-size:28px;margin-bottom:8px;">📖</div>
                                  <div style="color:#fff;font-weight:700;font-size:14px;margin-bottom:4px;">Browse Courses</div>
                                  <div style="color:#7070a0;font-size:12px;">Explore 100s of courses</div>
                                </div>
                              </td>
                              <td width="50%%" style="padding:0 0 16px 8px;">
                                <div style="background:rgba(255,255,255,0.04);border-radius:12px;padding:20px;
                                            border:1px solid rgba(108,99,255,0.2);">
                                  <div style="font-size:28px;margin-bottom:8px;">📝</div>
                                  <div style="color:#fff;font-weight:700;font-size:14px;margin-bottom:4px;">Assignments</div>
                                  <div style="color:#7070a0;font-size:12px;">Track your progress</div>
                                </div>
                              </td>
                            </tr>
                          </table>

                          <!-- CTA -->
                          <div style="text-align:center;margin-bottom:32px;">
                            <a href="http://localhost:5173"
                               style="display:inline-block;
                                      background:linear-gradient(135deg,#10b981,#06b6d4);
                                      color:#fff;font-size:16px;font-weight:700;
                                      text-decoration:none;padding:16px 48px;
                                      border-radius:50px;">
                              🚀 &nbsp; Go to Dashboard
                            </a>
                          </div>

                          <hr style="border:none;border-top:1px solid rgba(255,255,255,0.08);margin:0 0 24px;"/>
                          <p style="color:#7070a0;font-size:12px;text-align:center;margin:0;">
                            © 2025 Course Cloud · Happy learning! 🎓
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(userName);
    }

    private String buildOtpHtml(String userName, String otp) {
        // Split OTP into two groups of 3 for readability: 123 456
        String g1 = otp.substring(0, 3);
        String g2 = otp.substring(3, 6);
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"/><title>Your OTP – Course Cloud</title></head>
            <body style="margin:0;padding:0;background:#0f0f1a;font-family:'Segoe UI',Roboto,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f0f1a;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#1a1a2e;border-radius:16px;overflow:hidden;
                                border:1px solid rgba(108,99,255,0.25);
                                box-shadow:0 20px 60px rgba(0,0,0,0.5);">

                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,#6c63ff,#4facfe);padding:36px 48px;text-align:center;">
                        <div style="font-size:36px;margin-bottom:8px;">🔐</div>
                        <div style="color:#fff;font-size:24px;font-weight:800;">Email Verification</div>
                        <div style="color:rgba(255,255,255,0.75);font-size:13px;letter-spacing:2px;text-transform:uppercase;margin-top:4px;">Course Cloud</div>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:48px;">
                        <p style="color:#a0a0c0;font-size:15px;line-height:1.7;text-align:center;margin:0 0 32px;">
                          Hi <strong style="color:#c4b5fd;">%s</strong>! 👋<br/>
                          Use the code below to verify your email address.
                        </p>

                        <!-- OTP Box -->
                        <div style="text-align:center;margin-bottom:36px;">
                          <div style="display:inline-block;background:rgba(108,99,255,0.12);
                                      border:2px solid rgba(108,99,255,0.4);border-radius:16px;
                                      padding:24px 48px;">
                            <div style="font-size:42px;font-weight:900;letter-spacing:12px;
                                        color:#fff;font-family:'Courier New',monospace;
                                        text-shadow:0 0 20px rgba(108,99,255,0.6);">
                              %s &nbsp; %s
                            </div>
                            <div style="color:#a0a0c0;font-size:12px;margin-top:10px;letter-spacing:2px;">YOUR VERIFICATION CODE</div>
                          </div>
                        </div>

                        <!-- Expiry warning -->
                        <div style="background:rgba(245,158,11,0.08);border-left:3px solid #fbbf24;
                                    padding:12px 16px;border-radius:0 8px 8px 0;margin-bottom:36px;">
                          <p style="color:#fbbf24;font-size:13px;margin:0;font-weight:600;">⏰ This code expires in 10 minutes</p>
                          <p style="color:#7070a0;font-size:12px;margin:4px 0 0;">
                            If you did not create a Course Cloud account, please ignore this email.
                          </p>
                        </div>

                        <!-- Security note -->
                        <div style="background:rgba(255,255,255,0.03);border-radius:10px;padding:14px 18px;margin-bottom:28px;">
                          <p style="color:#7070a0;font-size:12px;margin:0;">
                            🔒 <strong style="color:#a0a0c0;">Security tip:</strong>
                            Never share this code with anyone. Course Cloud will never ask for your OTP via phone or chat.
                          </p>
                        </div>

                        <hr style="border:none;border-top:1px solid rgba(255,255,255,0.08);margin:0 0 24px;"/>
                        <p style="color:#7070a0;font-size:12px;text-align:center;margin:0;">
                          Need help? Contact
                          <a href="mailto:harshavardhan2286@gmail.com" style="color:#6c63ff;text-decoration:none;">harshavardhan2286@gmail.com</a>
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:rgba(0,0,0,0.2);padding:16px 48px;text-align:center;">
                        <p style="color:#5050a0;font-size:11px;margin:0;">© 2025 Course Cloud · All rights reserved</p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(userName, g1, g2);
    }
}
