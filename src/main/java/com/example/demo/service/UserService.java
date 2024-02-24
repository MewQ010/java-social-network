package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.entity.PersonalData;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.PersonalDataDTO;
import com.example.demo.entity.dto.UserDTO;
import com.example.demo.repository.*;
import com.example.demo.request.RegistrationRequest;
import com.example.demo.entity.VerificationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import static com.example.demo.constants.TextConstants.*;

@Service
@RequiredArgsConstructor
public class UserService{
    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final UserMapper mapper;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }


    public User registerUser(RegistrationRequest request) {
        List<UserDTO> user = this.findByEmail(request.getEmail());
        if (!user.isEmpty()) {
            throw new UserAlreadyExistsException(
                    USER_WITH_EMAIL_MESSAGE + request.getEmail() + ALREADY_EXIST_MESSAGE);
        }
        var newUserData = PersonalData.builder().email(request.getEmail()).build();
        userDataRepository.save(newUserData);
        var newUser =
                User.builder()
                        .login(request.getLogin())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .roles(roleRepository.findRoleByName(request.getRole()))
                        .personalData(newUserData)
                        .registrationDateTime(java.time.ZonedDateTime.now())
                        .build();
        return userRepository.save(newUser);
    }


    public List<UserDTO> findByEmail(String email) {
        PersonalDataDTO userData =
                mapper.personalDataToPersonalDataDto(userDataRepository.findByEmail(email));
        User user;
        if (userData == null) {
            return new ArrayList<>();
        } else {
            user = userRepository.findByPersonalData(mapper.personalDataDtoToPersonalData(userData));
        }
        return mapper.listOfUserToListOfUserDto((List<User>) user);
    }


    public void saveUserVerificationToken(UserDTO theUser, String token) {
        var verificationToken = new VerificationToken(token, mapper.userDtoToUser(theUser));
        tokenRepository.save(verificationToken);
    }


    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token == null) {
            return INVALID_VERIFICATION_TOKEN_LOG_MESSAGE;
        }
        UserDTO user = mapper.userToUserDto(token.getUser());
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
            tokenRepository.delete(token);
            return TOKEN_ALREADY_EXPIRED_MESSAGE;
        }
        user.setVerified(true);
        userRepository.save(mapper.userDtoToUser(user));
        return VALID_MESSAGE;
    }

    public void registerUser(User user) {
        if(userRepository.findByLogin(user.getLogin()) == null) {
            throw new UserAlreadyExistsException("Account already created on this UserName");
        }
        if(!userDataRepository.existsByEmail(user.getPersonalData().getEmail())) {
            throw new UserAlreadyExistsException("Account already created on this Email");
        } else {
            var newUserData = PersonalData.builder().email(user.getPersonalData().getEmail()).build();
            userDataRepository.save(newUserData);
            var newUser =
                    User.builder()
                            .login(user.getLogin())
//                            .password(passwordEncoder.encode(user.getPassword()))
                            .password(user.getPassword())
                            .personalData(newUserData)
                            .registrationDateTime(java.time.ZonedDateTime.now())
                            .build();
            userRepository.save(newUser);
        }
    }
    public void loginUser(User user) throws LoginException {
        User oldUser = userRepository.findByPersonalData(userDataRepository.findByEmail(user.getPersonalData().getEmail()));
//        if(!passwordEncoder.matches(user.getPassword(), oldUser.getPassword())) {
//            throw new LoginException("Wrong Login details " + user.getPassword().matches(oldUser.getPassword()));
//        }
        if(!user.getPassword().equals(oldUser.getPassword())) {
            throw new LoginException("Wrong Login details " + user.getPassword().matches(oldUser.getPassword()));
        }
    }

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

    public void sendMessage(PersonalData personalData) {
        String link = "http://localhost:8080/users/reset" + personalData.getEmail();
        MailSender mailSender = createMailSender();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(personalData.getEmail());
        message.setSubject("Confirm Email");
        message.setText("confirm your email with link " + link);
        mailSender.send(message);
    }

    public void resetPassword(String email, User newUser) {
        PersonalData personalData = userDataRepository.findByEmail(email);
        User user = userRepository.findByPersonalData(personalData);
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(user);
    }

    public void addPost(Post post) {
        postRepository.save(post);
    }
}
