package com.example.demo.validator;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserValidator {
    private final SwearWordsValidator swearWordsValidator;
    private final SpecialSymbolsValidator specialSymbolsValidator;
    private final LocalDateValidator localDateValidator;
    private final TelephoneValidator telephoneValidator;
    private final NotNullValidator notNullValidator;
    private final EmailValidator emailValidator;

    public void isValid(PersonalData personalData) {
        String firstName = swearWordsValidator.isValid( notNullValidator.isValid( personalData.getFirstName()) );
        String lastName = swearWordsValidator.isValid( notNullValidator.isValid(personalData.getLastName()) );
//        emailValidator.isValid(personalData.getEmail());
        specialSymbolsValidator.isValid(firstName);
        specialSymbolsValidator.isValid(lastName);
        localDateValidator.isValid(personalData.getDateOfBirth());
        telephoneValidator.isValid(personalData.getTelephone().getTelephoneNumber());
    }

    public void isValid(User user) {
        PersonalData personalData = user.getPersonalData();
        isValid(personalData);
        String login = swearWordsValidator.isValid( notNullValidator.isValid(user.getLogin()) );
        specialSymbolsValidator.isValid(login);
    }
}