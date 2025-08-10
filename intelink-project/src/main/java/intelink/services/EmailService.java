package intelink.services;

import intelink.services.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@intelink.com}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String link) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String content = "<html><body>" +
                "<h1>Welcome to Intelink!</h1>" +
                "<p>To complete your registration, please verify your email address by clicking the link below:</p>" +
                "<a href=\"" + link + "\">Verify Email</a>" +
                "</body></html>";

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Intelink Email Verification");
        helper.setText(content, true);

        mailSender.send(message);
    }

}
