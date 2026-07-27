package com.example.how2prompt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "how2prompt.mail")
public class MailProperties {

    /** Địa chỉ From (SMTP), ví dụ noreply@how2prompt.com */
    private String from = "noreply@how2prompt.local";

    /** Tên hiển thị người gửi */
    private String fromName = "How2Prompt";

    /**
     * Base URL frontend SPA — dùng ghép link verify email.
     * Ví dụ: http://localhost:5173
     */
    private String frontendBaseUrl = "http://localhost:5173";

    /** Path trên FE để xác minh email (query ?token=...) */
    private String verifyEmailPath = "/verify-email";

    /** TTL token verify trong Redis (mặc định 24h). */
    private Duration verificationTokenTtl = Duration.ofHours(24);

    /** Cooldown giữa 2 lần resend verification email. */
    private Duration resendCooldown = Duration.ofSeconds(60);
}
