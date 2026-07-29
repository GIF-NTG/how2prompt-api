package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Gửi email qua SMTP ({@link JavaMailSender}). @Async — không block luồng register.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    @Async
    public void sendEmailVerification(String toEmail, String fullName, String rawToken) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("Skip verification email: empty recipient");
            return;
        }

        String displayName = StringUtils.hasText(fullName) ? fullName.trim() : toEmail;
        String verifyUrl = buildVerifyUrl(rawToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setSubject("Xác minh email — How2Prompt");
            helper.setText(buildPlainText(displayName, verifyUrl), buildHtml(displayName, verifyUrl));

            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            // Không ném exception ra ngoài — register đã commit; chỉ log để retry/monitor sau
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendPasswordReset(String toEmail, String fullName, String rawToken) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("Skip password reset email: empty recipient");
            return;
        }

        String displayName = StringUtils.hasText(fullName) ? fullName.trim() : toEmail;
        String resetUrl = buildResetUrl(rawToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setSubject("Khôi phục mật khẩu — How2Prompt");
            helper.setText(buildResetPlainText(displayName, resetUrl), buildResetHtml(displayName, resetUrl));

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildResetUrl(String rawToken) {
        String base = mailProperties.getFrontendBaseUrl();
        String path = "/reset-password";
        if (!StringUtils.hasText(base) || !StringUtils.hasText(rawToken)) {
            return "";
        }
        return UriComponentsBuilder
                .fromUriString(trimTrailingSlash(base))
                .path(path)
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
    }

    private static String buildResetPlainText(String fullName, String resetUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào ").append(fullName).append(",\n\n");
        sb.append("Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản How2Prompt của bạn.\n");
        if (StringUtils.hasText(resetUrl)) {
            sb.append("Vui lòng mở link sau để thực hiện đặt lại mật khẩu mới (hết hạn sau 15 phút):\n");
            sb.append(resetUrl).append("\n\n");
        } else {
            sb.append("Vui lòng đặt lại mật khẩu trong ứng dụng.\n\n");
        }
        sb.append("Nếu bạn không yêu cầu khôi phục mật khẩu, hãy bỏ qua email này.\n\n");
        sb.append("— How2Prompt\n");
        return sb.toString();
    }

    private static String buildResetHtml(String fullName, String resetUrl) {
        String safeName = escapeHtml(fullName);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style=\"font-family:sans-serif;line-height:1.5;color:#111\">");
        sb.append("<p>Xin chào <strong>").append(safeName).append("</strong>,</p>");
        sb.append("<p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản <strong>How2Prompt</strong> của bạn.</p>");
        if (StringUtils.hasText(resetUrl)) {
            String safeUrl = escapeHtml(resetUrl);
            sb.append("<p>Vui lòng bấm nút bên dưới để khôi phục mật khẩu (hết hạn sau 15 phút):</p>");
            sb.append("<p><a href=\"").append(safeUrl).append("\" ");
            sb.append("style=\"display:inline-block;padding:10px 18px;background:#2563eb;color:#fff;");
            sb.append("text-decoration:none;border-radius:6px\">Đặt lại mật khẩu</a></p>");
            sb.append("<p style=\"font-size:12px;color:#666\">Hoặc mở link: <br/>");
            sb.append("<a href=\"").append(safeUrl).append("\">").append(safeUrl).append("</a></p>");
        } else {
            sb.append("<p>Vui lòng khôi phục mật khẩu trong ứng dụng.</p>");
        }
        sb.append("<p style=\"font-size:12px;color:#666\">Nếu bạn không yêu cầu khôi phục mật khẩu, hãy bỏ qua email này.</p>");
        sb.append("<p>— How2Prompt</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String buildVerifyUrl(String rawToken) {
        String base = mailProperties.getFrontendBaseUrl();
        String path = mailProperties.getVerifyEmailPath();
        if (!StringUtils.hasText(base) || !StringUtils.hasText(rawToken)) {
            return "";
        }
        return UriComponentsBuilder
                .fromUriString(trimTrailingSlash(base))
                .path(path.startsWith("/") ? path : "/" + path)
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
    }

    private static String trimTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static String buildPlainText(String fullName, String verifyUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào ").append(fullName).append(",\n\n");
        sb.append("Cảm ơn bạn đã đăng ký How2Prompt.\n");
        if (StringUtils.hasText(verifyUrl)) {
            sb.append("Vui lòng mở link sau để xác minh email (hết hạn sau một thời gian):\n");
            sb.append(verifyUrl).append("\n\n");
        } else {
            sb.append("Vui lòng xác minh email trong ứng dụng.\n\n");
        }
        sb.append("Nếu bạn không đăng ký tài khoản này, hãy bỏ qua email.\n\n");
        sb.append("— How2Prompt\n");
        return sb.toString();
    }

    private static String buildHtml(String fullName, String verifyUrl) {
        String safeName = escapeHtml(fullName);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style=\"font-family:sans-serif;line-height:1.5;color:#111\">");
        sb.append("<p>Xin chào <strong>").append(safeName).append("</strong>,</p>");
        sb.append("<p>Cảm ơn bạn đã đăng ký <strong>How2Prompt</strong>.</p>");
        if (StringUtils.hasText(verifyUrl)) {
            String safeUrl = escapeHtml(verifyUrl);
            sb.append("<p>Vui lòng bấm nút bên dưới để xác minh email:</p>");
            sb.append("<p><a href=\"").append(safeUrl).append("\" ");
            sb.append("style=\"display:inline-block;padding:10px 18px;background:#2563eb;color:#fff;");
            sb.append("text-decoration:none;border-radius:6px\">Xác minh email</a></p>");
            sb.append("<p style=\"font-size:12px;color:#666\">Hoặc mở link: <br/>");
            sb.append("<a href=\"").append(safeUrl).append("\">").append(safeUrl).append("</a></p>");
        } else {
            sb.append("<p>Vui lòng xác minh email trong ứng dụng.</p>");
        }
        sb.append("<p style=\"font-size:12px;color:#666\">Nếu bạn không đăng ký tài khoản này, hãy bỏ qua email.</p>");
        sb.append("<p>— How2Prompt</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
