package com.example.demo.validator;

import com.example.demo.entity.PersonalData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

@RequiredArgsConstructor
@Component
public class PersonalDataValidator {
    private final SwearWordsValidator swearWordsValidator;
    private final SpecialSymbolsValidator specialSymbolsValidator;
    private final LocalDateValidator localDateValidator;
    private final TelephoneValidator telephoneValidator;

    public void isValid(PersonalData personalData) {
        String firstName = swearWordsValidator.isValid(personalData.getFirstName());
        String lastName = swearWordsValidator.isValid(personalData.getLastName());
        specialSymbolsValidator.isValid(firstName);
        specialSymbolsValidator.isValid(lastName);
        localDateValidator.isValid(personalData.getDateOfBirth());
        telephoneValidator.isValid(personalData.getTelephone().getTelephoneNumber());
    }
}