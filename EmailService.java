package com.example.kaisi_lagi.UserMaster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@kaisilagi.com}")
    private String fromEmail;

    /**
     * Send beautiful responsive OTP email (Stravia-inspired design)
     */
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Identity - Kaisi Lagi");

            String htmlBody = """
                        <!DOCTYPE html>
                                        <html lang="en">
                                        <head>
                                            <meta charset="UTF-8">
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                            <meta http-equiv="X-UA-Compatible" content="IE=edge">
                                            <title>Verify Your Identity - Kaisi Lagi</title>
                                            <style>
                                                @media only screen and (max-width: 600px) {
                                                    .email-container {
                                                        width: 100% !important;
                                                        padding: 4px !important;
                                                    }
                                                    .content-wrapper {
                                                        padding: 20px !important;
                                                    }
                                                    .hero-title {
                                                        font-size: 24px !important;
                                                    }
                                                    .otp-code {
                                                        font-size: 32px !important;
                                                        letter-spacing: 8px !important;
                                                    }
                                                    .leaves-section {
                                                        padding: 20px !important;
                                                    }
                                                    .leaf-emoji {
                                                        font-size: 30px !important;
                                                        margin: 0 10px !important;
                                                    }
                                                    .thank-you-title {
                                                        font-size: 20px !important;
                                                    }
                                                    .app-badges {
                                                        display: block !important;
                                                    }
                                                    .app-badge {
                                                        display: block !important;
                                                        margin: 5px auto !important;
                                                        max-width: 200px !important;
                                                    }
                                                    .social-icon {
                                                        margin: 0 4px !important;
                                                    }
                                                    .footer-links {
                                                        font-size: 10px !important;
                                                    }
                                                }

                                                @media only screen and (max-width: 480px) {
                                                    .hero-title {
                                                        font-size: 20px !important;
                                                    }
                                                    .otp-code {
                                                        font-size: 28px !important;
                                                        letter-spacing: 6px !important;
                                                    }
                                                    .content-padding {
                                                        padding: 15px 20px !important;
                                                    }
                                                }
                                            </style>
                                        </head>
                                        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">

                                            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="margin: 0; padding: 20px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 0;">

                                                        <table role="presentation" class="email-container" cellpadding="0" cellspacing="0" border="0" style="max-width: 600px; width: 100%; margin: 0 auto; background: linear-gradient(180deg, #FFD700 0%, #FFA500 100%); padding: 8px; border-radius: 16px;">
                                                            <tr>
                                                                <td style="padding: 0;">
                                                                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="width: 100%; background-color: #ffffff; border-radius: 12px; overflow: hidden;">

                                                                        <!-- Header -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 30px 30px 20px; text-align: center; background-color: #fff;">
                                                                                <div style="display: inline-block; background: linear-gradient(135deg, #fc8601 0%, #c40941e3 100%); padding: 12px 24px; border-radius: 25px; margin-bottom: 20px;">
                                                                                    <span style="color: #ffffff; font-size: 18px; font-weight: bold; letter-spacing: 2px;">KAISI LAGI</span>
                                                                                </div>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Hero Section -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 20px 40px; text-align: center; background: linear-gradient(180deg, #FFF9E6 0%, #FFFFFF 100%);">
                                                                                <div style="font-size: 48px; margin-bottom: 15px;">🔒</div>
                                                                                <h1 class="hero-title" style="margin: 0 0 15px; font-size: 32px; font-weight: 700; color: #1a1a1a; line-height: 1.3;">
                                                                                    Verify your<br>identity in<br>seconds
                                                                                </h1>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Greeting -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 20px 40px 10px; color: #666; font-size: 14px;">
                                                                                <p style="margin: 0;">Hi There! 👋</p>
                                                                                <p style="margin: 5px 0 0;">Your one-time password (OTP) is:</p>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- OTP Code -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 20px 40px;">
                                                                                <div style="background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%); border: 3px dashed #667eea; border-radius: 12px; padding: 25px; text-align: center;">
                                                                                    <div class="otp-code" style="font-size: 42px; font-weight: bold; letter-spacing: 12px; color: #1a1a1a; font-family: 'Courier New', monospace; word-wrap: break-word;">
                                                                                        OTP_PLACEHOLDER
                                                                                    </div>
                                                                                </div>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Validity Info -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 10px 40px 20px; color: #666; font-size: 13px; line-height: 1.6;">
                                                                                <p style="margin: 0;">This code is valid for the next <strong>2 minutes</strong>. Please use it to complete your verification process.</p>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Security Warning -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 20px 40px;">
                                                                                <div style="background: #FFF3CD; border-left: 4px solid #FFC107; padding: 15px; border-radius: 8px;">
                                                                                    <p style="margin: 0; color: #856404; font-size: 13px; line-height: 1.6;">
                                                                                        For security purposes, never share this code with anyone. If you didn't request this, please contact our support team immediately.
                                                                                    </p>
                                                                                </div>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Thank You Section with Leaves -->
                                                                        <tr>
                                                                            <td class="leaves-section content-padding" style="padding: 30px 40px; text-align: center; background: linear-gradient(180deg, #FFFFFF 0%, #FFF9E6 100%);">
                                                                                <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto;">
                                                                                    <tr>
                                                                                        <td class="leaf-emoji" style="vertical-align: middle; font-size: 40px; padding: 0 10px;">🍂</td>
                                                                                        <td style="vertical-align: middle; text-align: center; padding: 0 10px;">
                                                                                            <h2 class="thank-you-title" style="margin: 0 0 8px; font-size: 24px; color: #1a1a1a; line-height: 1.3;">
                                                                                                Thank you for<br>choosing <span style="color: #FF8C00;">Kaisi Lagi</span>
                                                                                            </h2>
                                                                                            <p style="margin: 0; color: #666; font-size: 13px; line-height: 1.5;">
                                                                                                If you have questions, feel free to<br>reply to this email or visit our<br><strong style="color: #667eea;">Customer support</strong>.
                                                                                            </p>
                                                                                        </td>
                                                                                        <td class="leaf-emoji" style="vertical-align: middle; font-size: 40px; padding: 0 10px;">🍂</td>
                                                                                    </tr>
                                                                                </table>
                                                                            </td>
                                                                        </tr>

                                                                        <!-- Footer -->
                                                                        <tr>
                                                                            <td class="content-padding" style="padding: 30px 40px; background-color: #f8f9fa; border-top: 1px solid #e9ecef;">
                                                                                <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="width: 100%;">
                                                                                    <!-- Logo -->
                                                                                    <tr>
                                                                                        <td style="text-align: center; padding-bottom: 20px;">
                                                                                            <div style="display: inline-block; background: linear-gradient(135deg, #fc8601 0%, #c40941e3 100%); padding: 10px 20px; border-radius: 20px; margin-bottom: 15px;">
                                                                                                <span style="color: #ffffff; font-size: 16px; font-weight: bold; letter-spacing: 1px;">KAISI LAGI</span>
                                                                                            </div>
                                                                                        </td>
                                                                                    </tr>

                                                                                    <!-- App Download Section -->
                                                                                    <tr>
                                                                                        <td style="text-align: center; color: #999; font-size: 12px; line-height: 1.6; padding-bottom: 15px;">
                                                                                            <p style="margin: 0 0 10px;">Download Kaisi Lagi App on</p>
                                                                                            <div class="app-badges" style="margin: 10px 0;">
                                                                                                <span class="app-badge" style="display: inline-block; margin: 5px; padding: 8px 16px; background: #f0f0f0; border-radius: 6px; font-size: 11px; color: #666; white-space: nowrap;">
                                                                                                    📱 Play Store
                                                                                                </span>
                                                                                                <span class="app-badge" style="display: inline-block; margin: 5px; padding: 8px 16px; background: #f0f0f0; border-radius: 6px; font-size: 11px; color: #666; white-space: nowrap;">
                                                                                                    🍎 App Store
                                                                                                </span>
                                                                                            </div>
                                                                                            <p style="margin: 10px 0 0;">to receive monthly promos<br>and updates.</p>
                                                                                        </td>
                                                                                    </tr>

                                                                                    <!-- Social Icons -->
                                                                                    <tr>
                                                                                        <td style="text-align: center; padding: 15px 0;">
                                                                                            <a href="#" class="social-icon" style="display: inline-block; margin: 0 8px; width: 32px; height: 32px; background: #667eea; border-radius: 50%; text-decoration: none; line-height: 32px; color: #fff; font-size: 14px;">f</a>
                                                                                            <a href="#" class="social-icon" style="display: inline-block; margin: 0 8px; width: 32px; height: 32px; background: #667eea; border-radius: 50%; text-decoration: none; line-height: 32px; color: #fff; font-size: 14px;">𝕏</a>
                                                                                            <a href="#" class="social-icon" style="display: inline-block; margin: 0 8px; width: 32px; height: 32px; background: #667eea; border-radius: 50%; text-decoration: none; line-height: 32px; color: #fff; font-size: 14px;">▶</a>
                                                                                        </td>
                                                                                    </tr>

                                                                                    <!-- Footer Links -->
                                                                                    <tr>
                                                                                        <td class="footer-links" style="text-align: center; color: #999; font-size: 11px; padding-top: 15px; border-top: 1px solid #e0e0e0; line-height: 1.6;">
                                                                                            <p style="margin: 0 0 5px;">Want to change which emails you receive from us? You can <a href="#" style="color: #667eea; text-decoration: none;">update your preferences</a> or <a href="#" style="color: #667eea; text-decoration: none;">unsubscribe</a>.</p>
                                                                                            <p style="margin: 5px 0;">You can view our <a href="#" style="color: #667eea; text-decoration: none;">privacy policy</a>.</p>
                                                                                            <p style="margin: 10px 0 0;">© 2026 Kaisi Lagi. All rights reserved.</p>
                                                                                        </td>
                                                                                    </tr>
                                                                                </table>
                                                                            </td>
                                                                        </tr>

                                                                    </table>
                                                                </td>
                                                            </tr>
                                                        </table>

                                                    </td>
                                                </tr>
                                            </table>

                                        </body>
                                        </html>
                    """;

            // Replace placeholder with actual OTP
            String formattedHtml = htmlBody.replace("OTP_PLACEHOLDER", otp);
            helper.setText(formattedHtml, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException | MailException e) {
            System.err.println("Failed to send OTP email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Generic email method for other purposes (backward compatible)
     */
    public void sendEmail(String to, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, false);

            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}