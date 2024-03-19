package com.example.demo.validator;

import com.example.demo.exception.SpecialSymbolsException;
import org.springframework.stereotype.Component;

@Component
public class SpecialSymbolsValidator{

    public void isValid(String info) throws SpecialSymbolsException {

        String specialSymbols = "1234567890-=+_)(*&^%$#@!\\|/}{[]?.><,№'\"";
        for(String i : specialSymbols.split("")) {
            if(info.contains(i)) {
                throw new SpecialSymbolsException("Special symbols are not allowed in Data");
            }
        }

    }
}