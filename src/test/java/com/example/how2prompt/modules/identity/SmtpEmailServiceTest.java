package com.example.how2prompt.modules.identity;

import com.example.how2prompt.config.MailProperties;
import com.example.how2prompt.modules.identity.service.SmtpEmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setUp() {
        lenient().when(mailProperties.getFrom()).thenReturn("noreply@how2prompt.com");
        lenient().when(mailProperties.getFromName()).thenReturn("How2Prompt");
        lenient().when(mailProperties.getFrontendBaseUrl()).thenReturn("http://localhost:3000/");
        lenient().when(mailProperties.getVerifyEmailPath()).thenReturn("/verify-email");
    }

    @Test
    void testSendEmailVerification_Success() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendEmailVerification("test@example.com", "Test User", "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailVerification_EmptyEmail() {
        smtpEmailService.sendEmailVerification("", "Test User", "raw-token");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailVerification_NullFullName() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendEmailVerification("test@example.com", null, "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailVerification_EmptyToken() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendEmailVerification("test@example.com", "Test User", "");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    void testSendEmailVerification_EmptyBaseUrl() throws Exception {
        when(mailProperties.getFrontendBaseUrl()).thenReturn("");
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendEmailVerification("test@example.com", "Test User", "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailVerification_ThrowsMailException() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // Should catch the exception and not throw it outwards
        smtpEmailService.sendEmailVerification("test@example.com", "Test User", "raw-token");
    }

    @Test
    void testSendPasswordReset_Success() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendPasswordReset("test@example.com", "Test User", "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordReset_EmptyEmail() {
        smtpEmailService.sendPasswordReset(null, "Test User", "raw-token");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
    
    @Test
    void testSendPasswordReset_NullFullName() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendPasswordReset("test@example.com", null, "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordReset_EmptyToken() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendPasswordReset("test@example.com", "Test User", null);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    void testSendPasswordReset_EmptyBaseUrl() throws Exception {
        when(mailProperties.getFrontendBaseUrl()).thenReturn("");
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendPasswordReset("test@example.com", "Test User", "raw-token");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordReset_ThrowsException() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        smtpEmailService.sendPasswordReset("test@example.com", "Test User", "raw-token");
    }
}
