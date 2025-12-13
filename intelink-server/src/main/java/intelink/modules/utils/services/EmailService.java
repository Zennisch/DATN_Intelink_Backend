package intelink.modules.utils.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:no-reply@intelink.com}")
    private String fromEmail;

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String link) throws MessagingException {
        Context context = new Context();
        context.setVariable("verificationLink", link);

        String html = templateEngine.process("email/verification", context);
        sendHtmlEmail(to, "Intelink Email Verification", html);
    }

    public void sendResetPasswordEmail(String to, String link) throws MessagingException {
        Context context = new Context();
        context.setVariable("resetLink", link);

        String html = templateEngine.process("email/reset-password", context);
        sendHtmlEmail(to, "Intelink Password Reset", html);
    }

}
