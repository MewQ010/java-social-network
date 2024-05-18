package com.example.demo.service;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.Telephone;
import com.example.demo.entity.TelephoneCode;
import com.example.demo.entity.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void registerUser() {
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        TelephoneCode telephoneCode = new TelephoneCode();
        telephoneCode.setCode("+380");
        ZonedDateTime registrationTime = java.time.ZonedDateTime.now();
        var telephone =
                Telephone.builder()
                        .telephoneNumber(12345678L)
                        .telephoneCode(telephoneCode)
                        .build();
        var personalData =
                PersonalData.builder()
                        .email("efifnefi#gmail.com")
                        .telephone(telephone)
                        .lastName("Prosiannikov")
                        .firstName("Sasha")
                        .dateOfBirth(dateOfBirth)
                        .profileImageUrl("def-profile-image.jpg")
                        .description("Hello")
                        .build();
        var newUser =
                User.builder()
                        .login("rgrtrgr")
                        .password(passwordEncoder.encode( "hefiow"))
                        .personalData(personalData)
                        .registrationDateTime(registrationTime)
                        .build();
        Assertions.assertEquals(newUser, userService.registerUser(newUser));

    }
}