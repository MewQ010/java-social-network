package com.example.demo.validator;

import com.example.demo.exception.LoginException;
import org.springframework.stereotype.Component;

@Component
public class NotNullValidator {

    public String isValid(String data) throws LoginException {
        if(data.isEmpty()) {
            throw new LoginException("Data must not be null");
        }
        return data;
    }
}
