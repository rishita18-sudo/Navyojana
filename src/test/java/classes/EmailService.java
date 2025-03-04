package classes;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailService {
    private final String senderEmail = "testmail4rnd@gmail.com";
    private final String senderPassword = "iqmzwumlwsytmeqp";  // App Password
    private final String smtpHost = "smtp.gmail.com";
    private final int smtpPort = 587;

    public void sendEmailWithAttachment(String recipientEmail, String subject, String reportFilePath, String zipFilePath, int passed, int total) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            String summaryMessage = String.format("Test Execution Summary:\n\n✔ %d out of %d PASSED\n\nAttached: Test Report & Execution Files.", passed, total);
            
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(summaryMessage);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (reportFilePath != null) {
                MimeBodyPart reportAttachment = new MimeBodyPart();
                reportAttachment.attachFile(new File(reportFilePath));
                multipart.addBodyPart(reportAttachment);
            }

            if (zipFilePath != null) {
                MimeBodyPart zipAttachment = new MimeBodyPart();
                zipAttachment.attachFile(new File(zipFilePath));
                multipart.addBodyPart(zipAttachment);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("📧 Email sent successfully with attachments.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
