package intelink.services.interfaces;

import jakarta.mail.MessagingException;

public interface IEmailService {

    void sendVerificationEmail(String to, String link) throws MessagingException;

    void sendResetPasswordEmail(String to, String link) throws MessagingException;

}
