package com.example.demo.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        if ("The given id must not be null".equals(e.getMessage())) {
            return new RedirectView("/users/login"); //TODO make exception vue !
        }
        return new RedirectView("/error");
    }

}

