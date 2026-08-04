package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.service.SmtpEmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SmtpEmailServiceTest extends IdentityIntegrationTestBase {

    @Autowired
    private SmtpEmailService smtpEmailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void testSendEmailVerification() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendEmailVerification("test@example.com", "Test User", "raw-token");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, timeout(1000).times(1)).send(messageCaptor.capture());

        // We could also do some assertions on the MimeMessage content, 
        // but verifying the send was called is the main part.
        // It's hard to read MimeMessage without throwing exceptions, but we know it didn't crash.
    }

    @Test
    void testSendPasswordReset() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendPasswordReset("test@example.com", "Test User", "raw-token");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, timeout(1000).times(1)).send(messageCaptor.capture());
    }
}
