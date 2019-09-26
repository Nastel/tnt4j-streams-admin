package com.jkoolcloud.tnt4j.streams.admin.backend.emailSendinService;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

@Singleton
public class SendEmail {
    private static Logger LOG = Logger.getLogger(SendEmail.class);
    private static SendEmail single_instance = null;

    private boolean emailWithAttachment;
    private String recipientEmail;
    private String messageSubject;
    private String messageContent;
    private String attachmentFilePath; //Optional

    public SendEmail(boolean emailWithAttachment, String recipientEmail, String messageSubject, String messageContent, String attachmentFilePath){
        this.emailWithAttachment = emailWithAttachment;
        this.recipientEmail = recipientEmail;
        this.messageSubject = messageSubject;
        this.messageContent = messageContent;
        this.attachmentFilePath = attachmentFilePath;
    }

    private SendEmail(){
    }

    public static SendEmail Singleton()
    {
        if (single_instance == null) {
            single_instance = new SendEmail();
        }
        return single_instance;
    }

    public static void main(String [] args) {
        SendEmail emailService = Singleton();
        EmailMessageTemplate tempEmail = new EmailMessageTemplate();
//        tempEmail.getNewUserRegistration("Edvinas", "edvinasmas97@gmail.com", "https://www.gocypher.com/gocypher/");
        tempEmail.getUserPasswordReminder("Edvinas", "edvinasmas97@gmail.com");
        String senderEmail = "edvmas.sigleton@gmail.com"; //"test@localhost";
        String senderName =  "edvmas.sigleton@gmail.com"; //"test@localhost";
        String senderPassword = "naujasslb"; //"test";
        emailService.sendEmail(senderEmail, senderName,  senderPassword);
    }

    /**
     * A method that is used to send simple emails from gmail account through TLS or SSl protocol.
     * @param senderEmail The gmail email from which the data will be sent.
     * @param senderName Repeat the email for username field.
     * @param senderPassword The password for the user.
     */
    public void sendEmail(String senderEmail, String senderName, String senderPassword){
        try {
            //Session session = Session.getDefaultInstance(properties, null);
            Session session = mailClientSetUpConnection(senderName, senderPassword);
            Message message = createEmailMessage(session, senderEmail);
            Transport.send(message);
            LOG.info("Done");
        } catch (MessagingException e) {
            LOG.error("Problem on sending emails From: "+senderEmail+" To: "+recipientEmail);
            e.printStackTrace();
        }
    }

    private Properties EmailPropertiesWithAuth(){
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return properties;
    }

    private Properties EmailPropertiesForLocalSMTP(String senderName, String senderPassword){
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", "25");
        properties.put("mail.smtp.username", senderName);
        properties.put("mail.smtp.password", senderPassword);
        return properties;
    }

    private Session mailClientSetUpConnection(String senderName, String senderPassword){
        //You can choose to send the emails through:
            //  * Gmail EmailPropertiesWithAuth();
            //  * local SMTP client - EmailPropertiesForLocalSMTP(senderName, senderPassword);
        Properties properties = EmailPropertiesWithAuth();

        Session session = Session.getInstance(properties,
            new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderName, senderPassword);
                }
            });
        return session;
    }

    private Message createEmailMessage(Session session, String senderEmail){
        Message message = new MimeMessage(session);
        try {
            if(emailWithAttachment){
                message = addAttachmentToMessageBody(session);
            }else {
                message.setContent(messageContent, "text/html; charset=UTF-8");
            }
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(messageSubject);
        } catch (MessagingException e) {
            LOG.error("Problem on creating email message");
        }
        return message;

    }

    private Message addAttachmentToMessageBody(Session session){
        Message message = new MimeMessage(session);
        BodyPart messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setContent(messageContent, "text/html; charset=UTF-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            //Message part two: attachment
            messageBodyPart = new MimeBodyPart();
            String filename = attachmentFilePath;
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
        }catch (MessagingException e){
            e.printStackTrace();
        }
        return message;
    }

    public boolean isEmailWithAttachment() {
        return emailWithAttachment;
    }

    public void setEmailWithAttachment(boolean emailWithAttachment) {
        this.emailWithAttachment = emailWithAttachment;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getMessageSubject() {
        return messageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        this.messageSubject = messageSubject;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getAttachmentFilePath() {
        return attachmentFilePath;
    }

    public void setAttachmentFilePath(String attachmentFilePath) {
        this.attachmentFilePath = attachmentFilePath;
    }
}