package com.example.demo.validator;

import com.example.demo.exception.LoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailValidator {
    private final NotNullValidator notNullValidator;
    public void isValid(String email) throws LoginException {
        notNullValidator.isValid(email);
        if(!email.contains("@")) {
            throw new LoginException("Wrong Email");
        }
    }
}
