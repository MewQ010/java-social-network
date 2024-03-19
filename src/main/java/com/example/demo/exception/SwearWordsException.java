package com.example.demo.exception;

import com.example.demo.validator.SwearWordsValidator;

public class SwearWordsException extends RuntimeException{
    public SwearWordsException (String message) {
    super(message);
}
}
