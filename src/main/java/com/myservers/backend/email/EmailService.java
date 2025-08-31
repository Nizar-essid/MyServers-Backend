package com.myservers.backend.email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@RequiredArgsConstructor
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;
private final Environment env;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("support@e-satstore.com");
System.out.println();
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject,String template, Context context) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject(subject);
//        helper.setText(body);
        helper.setFrom("support@e-satstore.com");
        String htmlContent = templateEngine.process(template, context);
        helper.setText(htmlContent,true);

        System.out.println();
        mailSender.send(mimeMessage);
    }

    public void sendPasswordResetEmail(String email, String token,String name) {
        String password_reset_url = env.getProperty("webApplication.password_reset_url");
        LocalDateTime dateTime = LocalDateTime.now()
                .atZone(ZoneId.of("Europe/Paris")).toLocalDateTime();


        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("name", name);
        context.setVariable("subject","Password Reset Request");
        if (dateTime.getHour() < 12) {
            context.setVariable("greetings", "Bonjour cher(e) ");
        } else {
            context.setVariable("greetings", "Bonsoir cher(e) ");
        }
        context.setVariable("resetLink", password_reset_url+"/" + token + "/" + email);
       // System.out.println("Sending password reset email to: "+password_reset_url+"/" + token + "/" + email);

        try {
            sendHtmlEmail(email, "Password Reset Request", "password-reset-email", context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
}
