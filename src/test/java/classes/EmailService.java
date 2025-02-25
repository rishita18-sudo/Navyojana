package classes;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailService {
    private final String senderEmail = "testmail4rnd@gmail.com";
    private final String senderPassword = "iqmzwumlwsytmeqp";
    private final String smtpHost = "smtp.gmail.com";
    private final int smtpPort = 587;

    public void sendEmailWithAttachment(String recipientEmail, String subject, String reportPath, String zipFilePath, String summaryMessage) {
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

            // Create email body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(summaryMessage + "\n\nPlease find the attached test report.");

            // Attachments
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            addAttachment(multipart, reportPath);
            addAttachment(multipart, zipFilePath);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + recipientEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAttachment(Multipart multipart, String filePath) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(file);
            multipart.addBodyPart(attachmentPart);
        }
    }
}
