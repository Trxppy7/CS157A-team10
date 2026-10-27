
package pawmatch;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class PasswordResetEmail {

    public static void send(String recipient, String resetLink)
            throws MessagingException {

        String host = Db.setting("SMTP_HOST", "");
        String port = Db.setting("SMTP_PORT", "587");
        String username = Db.setting("SMTP_USER", "");
        String password = Db.setting("SMTP_PASSWORD", "");
        String from = Db.setting("SMTP_FROM", "");

        boolean localTest = host.equals("127.0.0.1") && port.equals("1025");

        if (host.isBlank() || from.isBlank() ||
                (!localTest && (username.isBlank() || password.isBlank()))) {
            throw new MessagingException("SMTP settings are incomplete.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", localTest ? "false" : "true");
        properties.put("mail.smtp.starttls.enable", localTest ? "false" : "true");
        properties.put("mail.smtp.starttls.required", localTest ? "false" : "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(recipient));
        message.setSubject("Reset Your PawMatch Password");
        message.setText(
                "We received a request to reset your PawMatch password.\n\n" +
                "Use the following link to set a new password:\n" +
                resetLink + "\n\n" +
                "This link expires in 30 minutes and can only be used once.\n\n" +
                "If you did not request a password reset, ignore this email."
        );

        Transport.send(message);
    }
}

