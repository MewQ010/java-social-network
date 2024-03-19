package com.example.demo.validator;

import com.example.demo.exception.SwearWordsException;
import com.example.demo.util.JSONUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SwearWordsValidator {

    public String isValid(String name) {

        ArrayList<String> arrSwearWords = JSONUtils.parse(JSONUtils.read("src/main/resources/swear.json", "swearWords"));

        name = name.toLowerCase();

        for(String i : arrSwearWords) {
            if(name.contains(i)) {
                throw new SwearWordsException("Bad Words in Data");
            }
        }

        return name;
    }
}