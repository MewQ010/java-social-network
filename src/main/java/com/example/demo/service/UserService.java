package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.entity.dto.PersonalDataDTO;
import com.example.demo.entity.dto.UserDTO;
import com.example.demo.repository.*;
import com.example.demo.request.RegistrationRequest;
import com.example.demo.validator.EmailValidator;
import com.example.demo.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.*;

import static com.example.demo.constants.TextConstants.*;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final TelephoneCodeRepository telephoneCodeRepository;
    private final UserValidator userValidator;
    private final TelephoneRepository telephoneRepository;
    private final EmailValidator emailValidator;
    private final CVRepository cvRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User registerUser(User user) throws UserAlreadyExistsException {
        userValidator.isValid(user);
        emailValidator.isValid(user.getPersonalData().getEmail());
        if(!userRepository.findByLogin(user.getLogin()).isEmpty()) {
            throw new UserAlreadyExistsException("Account already created on this UserName");
        }
        if(userDataRepository.existsByEmail(user.getPersonalData().getEmail())) {
            throw new UserAlreadyExistsException("Account already created on this email");
        }
            PersonalData personalData = user.getPersonalData();
            TelephoneCode newTelephoneCode = telephoneCodeRepository.findByCode(personalData.getTelephone().getTelephoneCode().getCode());
            Telephone telephone = Telephone.builder().telephoneNumber(personalData.getTelephone().getTelephoneNumber()).telephoneCode(newTelephoneCode).build();
            telephoneRepository.save(telephone);
            personalData.setTelephone(telephone);
            personalData.setProfileImageUrl("def-profile-img.jpg");
            userDataRepository.save(personalData);
            var newCV =
                    CV.builder()
                            .firstName(personalData.getFirstName())
                            .lastName(personalData.getLastName())
                            .github_link("")
                            .instagram_link("")
                            .telegram_link("")
                            .description("This user doesn't made his cv yet")
                            .image_url(null)
                            .build();
            cvRepository.save(newCV);
            var newUser =
                    User.builder()
                            .login(user.getLogin())
                            .password(passwordEncoder.encode(user.getPassword()))
                            .personalData(personalData)
                            .registrationDateTime(java.time.ZonedDateTime.now())
                            .role(UserRole.USER)
                            .cv(newCV)
                            .build();
            userRepository.save(newUser);
        return newUser;
    }
    public void loginUser(User user) throws LoginException {
        if(!userRepository.existsByLogin(user.getLogin())) {
            throw new LoginException("We can't find user with this User Name");
        }
        User oldUser = userRepository.findByLogin(user.getLogin()).get(0);
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

    public void savePersonalData(PersonalData exitingPersonalData, PersonalData newPersonalData, String profileImage) {
        TelephoneCode newTelephoneCode = telephoneCodeRepository.findByCode(newPersonalData.getTelephone().getTelephoneCode().getCode());
        Telephone telephone = Telephone.builder().telephoneNumber(newPersonalData.getTelephone().getTelephoneNumber()).telephoneCode(newTelephoneCode).build();
        telephoneRepository.save(telephone);
        userValidator.isValid(newPersonalData);

        exitingPersonalData.setFirstName(newPersonalData.getFirstName());
        exitingPersonalData.setLastName(newPersonalData.getLastName());
        exitingPersonalData.setDateOfBirth(newPersonalData.getDateOfBirth());
        exitingPersonalData.setProfileImageUrl(profileImage);
        exitingPersonalData.setTelephone(telephone);
        exitingPersonalData.setDescription(newPersonalData.getDescription());

        userDataRepository.save(exitingPersonalData);
    }

    public void savePersonalData(PersonalData exitingPersonalData, PersonalData newPersonalData) {
        TelephoneCode newTelephoneCode = telephoneCodeRepository.findByCode(newPersonalData.getTelephone().getTelephoneCode().getCode());
        Telephone telephone = Telephone.builder().telephoneNumber(newPersonalData.getTelephone().getTelephoneNumber()).telephoneCode(newTelephoneCode).build();
        telephoneRepository.save(telephone);
        userValidator.isValid(newPersonalData);

        exitingPersonalData.setFirstName(newPersonalData.getFirstName());
        exitingPersonalData.setLastName(newPersonalData.getLastName());
        exitingPersonalData.setDateOfBirth(newPersonalData.getDateOfBirth());
        exitingPersonalData.setTelephone(telephone);
        exitingPersonalData.setDescription(newPersonalData.getDescription());

        userDataRepository.save(exitingPersonalData);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.findByLoginStartingWithIgnoreCase(keyword);
    }

}
