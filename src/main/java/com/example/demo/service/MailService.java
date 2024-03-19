package com.example.demo.service;

import com.example.demo.entity.PersonalData;
import com.example.demo.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Properties;

@Service
@AllArgsConstructor
public class MailService {

    private final UserDataRepository userDataRepository;

    public JavaMailSender createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("java.intita@gmail.com");
        mailSender.setPassword("csug gyry cmzr yeeb");


        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", 587);

        return mailSender;
    }

    public void sendMessage(PersonalData personalData) throws LoginException {
        if(!userDataRepository.existsByEmail(personalData.getEmail())) {
            throw new LoginException("We can't find user with this email");
        } else {
            String link = "http://localhost:8080/users/changePassword/" + personalData.getEmail();
            MailSender mailSender = createMailSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(personalData.getEmail());
            message.setSubject("Confirm Email");
            message.setText("confirm your email with link " + link);
            mailSender.send(message);
        }
    }

}
