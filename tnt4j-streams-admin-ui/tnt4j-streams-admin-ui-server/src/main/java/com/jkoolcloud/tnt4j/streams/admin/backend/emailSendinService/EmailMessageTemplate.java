package com.jkoolcloud.tnt4j.streams.admin.backend.emailSendinService;

public class EmailMessageTemplate {

    private SendEmail emailService = SendEmail.Singleton();

    public EmailMessageTemplate (){}

    public void getNewUserRegistration(String name, String recipientEmail, String emailConfirmationLink){
      //"tomas.jurevic@singleton-labs.lt"; //"sender@localhost";
        String messageSubject = "Confirm user registration";
        String messageContent = "<meta http-equiv=\"Content-Type\"  content=\"text/html charset=UTF-8\" />\n" +
                "<img src=\"https://image.shutterstock.com/image-photo/large-beautiful-drops-transparent-rain-260nw-668593321.jpg\"><img>\n" +
                "<div style=\"color:#000000;\">" +
                "<h2>Hello "+name+",</h2>\n" +
                "  <br>\n" +
                "  <h3>Welcome to GoCypher!</h3>\n" +
                "  <p style=\"font-size:14px;\"> Only the last step left! <br><br> Confirm your e-mail address to be able to <br> login and use our services. </p>\n" +
                "  <div style=\"margin-top: 30px;\"><a style=\"border-radius: 20px; height: 40px; width: 150px; background-color: #489431; " +
                "font-weight: bold; color: #ffffff; padding: 10px; margin: 20px;\" " +
                "href="+emailConfirmationLink+"> Confirm E-mail </a></div>\n" +
                "  <p style=\"font-size:12px;\"><br> If you are having trouble clicking the confirmation button, try using the URL below.<br><br>"+emailConfirmationLink+"</p>" +
                "</div>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }

    public void getUserPasswordReminder(String name, String recipientEmail){
         //"sender@localhost";
        String messageSubject = "Password reset";
        String messageContent = "<meta http-equiv=\"Content-Type\"  content=\"text/html charset=UTF-8\" />\n" +
                "<img src=\"https://image.shutterstock.com/image-photo/large-beautiful-drops-transparent-rain-260nw-668593321.jpg\"><img>\n" +
                "<div style=\"color:#000000;\">" +
                "<h2>Hello "+name+",</h2>\n" +
                "  <br>\n" +
                "  <h3>You have forgotten your password? No worries! :)</h3> \n" +
                "  <h5 style=\"font-size:14px;\">A request was received to change the pasword for your GoCypher account </h5>\n" +
                "  <p style=\"font-size:14px;\">If you which to proceed <b>click</b> the button below and a new password will be generated and sent to you shortly.</p>\n" +
                "  <div style=\"margin-top: 30px;\"><a style=\"border-radius: 20px; height: 40px; width: 150px; background-color: #489431; font-weight: bold; color: #ffffff; padding: 10px; margin: 20px;\" href=\"https://www.gocypher.com/gocypher/\"> Reset password </a>\n" +
                "  </div>\n" +
                "  <br><br>\n" +
                "  <p style=\"font-size:14px;\">If <b>you</b> did not request a new password, please let us know by replying to this email.</p>\n" +
                "  \n" +
                "  \n" +
                "  <p style=\"font-size:12px;\"><br> If you are having trouble clicking the reset button, try using the URL below.<br><br>https://www.gocypher.com/gocypher/</p>" +
                " </div>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }

    public void getUserReacuringPaymentInvoice(String recipientEmail){
        recipientEmail = "edvinasmas97@gmail.com"; //"sender@localhost";
        String messageSubject = "Testing";
        String messageContent = "<h1>This is actual message</h1> <br><br> " +
                "<h3> And this is the actual message children text </h3> " +
                "<span>Please don't spam me mister spam bot</span>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }

    public void getUserSuccessfulPayment(String recipientEmail){
        recipientEmail = "edvinasmas97@gmail.com"; //"sender@localhost";
        String messageSubject = "Testing";
        String messageContent = "<h1>This is actual message</h1> <br><br> " +
                "<h3> And this is the actual message children text </h3> " +
                "<span>Please don't spam me mister spam bot</span>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }

    public void getUserSuccessfulServicesCanceling(String recipientEmail){
        recipientEmail = "edvinasmas97@gmail.com"; //"sender@localhost";
        String messageSubject = "Testing";
        String messageContent = "<h1>This is actual message</h1> <br><br> " +
                "<h3> And this is the actual message children text </h3> " +
                "<span>Please don't spam me mister spam bot</span>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }

    public void getUserSuccessfulCreditCardAdding(String recipientEmail){
        recipientEmail = "edvinasmas97@gmail.com"; //"sender@localhost";
        String messageSubject = "Testing";
        String messageContent = "<h1>This is actual message</h1> <br><br> " +
                "<h3> And this is the actual message children text </h3> " +
                "<span>Please don't spam me mister spam bot</span>";
        emailService.setEmailWithAttachment(false);
        emailService.setRecipientEmail(recipientEmail);
        emailService.setMessageSubject(messageSubject);
        emailService.setMessageContent(messageContent);
    }
}
