package com.example.how2prompt.modules.identity.service;

/**
 * Gửi email (verify, ...). Implement mặc định: {@link SmtpEmailService} qua SMTP.
 */
public interface EmailService {

    /**
     * Gửi email xác minh. Chạy async — không throw về caller register.
     *
     * @param toEmail  người nhận
     * @param fullName tên hiển thị
     * @param rawToken token verify (gắn vào link FE); lưu hash trong Redis
     */
    void sendEmailVerification(String toEmail, String fullName, String rawToken);

    /**
     * Gửi email khôi phục mật khẩu. Chạy async.
     *
     * @param toEmail  người nhận
     * @param fullName tên hiển thị
     * @param rawToken token reset password
     */
    void sendPasswordReset(String toEmail, String fullName, String rawToken);
}
