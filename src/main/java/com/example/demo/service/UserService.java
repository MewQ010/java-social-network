package com.example.demo.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.entity.*;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.entity.dto.PersonalDataDTO;
import com.example.demo.entity.dto.UserDTO;
import com.example.demo.repository.*;
import com.example.demo.request.RegistrationRequest;
import com.example.demo.validator.PersonalDataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    private final TelephoneCodeRepository telephoneCodeRepository;
    private final PersonalDataValidator personalDataValidator;
    private final TelephoneRepository telephoneRepository;


    @Autowired
    S3Service s3Service;

    @Autowired
    AmazonS3 amazonS3;

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

    public void registerUser(User user) throws UserAlreadyExistsException, LoginException {
        personalDataValidator.isValid(user.getPersonalData());
        if(!userRepository.findByLogin(user.getLogin()).isEmpty()) {
            throw new UserAlreadyExistsException("Account already created on this UserName");
        }
        if(userDataRepository.existsByEmail(user.getPersonalData().getEmail())) {
            throw new LoginException();
        } else {
            var newUserData = PersonalData.builder().email(user.getPersonalData().getEmail()).profileImageUrl("def-profile-img.jpg").build();
            userDataRepository.save(newUserData);
            var newUser =
                    User.builder()
                            .login(user.getLogin())
                            .password(passwordEncoder.encode(user.getPassword()))
                            .personalData(newUserData)
                            .registrationDateTime(java.time.ZonedDateTime.now())
                            .build();
            userRepository.save(newUser);
        }
    }
    public void loginUser(User user) throws LoginException {
        if(!userDataRepository.existsByEmail(user.getPersonalData().getEmail())) {
            throw new LoginException("We can't find user with this email");
        }
        User oldUser = userRepository.findByPersonalData(userDataRepository.findByEmail(user.getPersonalData().getEmail()));
        if(!passwordEncoder.matches(user.getPassword(), oldUser.getPassword())) {
            throw new LoginException("Wrong Password");
        }
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

    public void savePersonalData(PersonalData exitingPersonalData, PersonalData newPersonalData, String profileImage) throws LoginException {
        TelephoneCode newTelephoneCode = telephoneCodeRepository.findByCode(newPersonalData.getTelephone().getTelephoneCode().getCode());
        Telephone telephone = Telephone.builder().telephoneNumber(newPersonalData.getTelephone().getTelephoneNumber()).telephoneCode(newTelephoneCode).build();
        telephoneRepository.save(telephone);
        personalDataValidator.isValid(newPersonalData);

        exitingPersonalData.setFirstName(newPersonalData.getFirstName());
        exitingPersonalData.setLastName(newPersonalData.getLastName());
        exitingPersonalData.setDateOfBirth(newPersonalData.getDateOfBirth());
        exitingPersonalData.setProfileImageUrl(profileImage);
        exitingPersonalData.setTelephone(telephone);
        exitingPersonalData.setDescription(newPersonalData.getDescription());

        userDataRepository.save(exitingPersonalData);
    }
}
