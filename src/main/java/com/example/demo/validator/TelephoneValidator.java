package com.example.demo.validator;

import com.example.demo.exception.TelephoneException;
import org.springframework.stereotype.Component;

@Component
public class TelephoneValidator {

    public void isValid(Long telephoneNumber) {
        int telephoneLength = telephoneNumber.toString().length();
        if(telephoneLength <= 12 && telephoneLength > 7) {
        } else{
            throw new TelephoneException("Invalid Telephone Number");
        }
    }

}