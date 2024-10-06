package com.example.demo.controller;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.TelephoneCode;
import com.example.demo.entity.User;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.HttpCookie;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class RegistrationController {

    private final UserService userService;
    private final TelephoneCodeRepository telephoneCodeRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @GetMapping("/register")
    public String createUser(Model model) {

        model.addAttribute("telephone_codes", telephoneCodeRepository.findAll());
        model.addAttribute("telephone_code", new TelephoneCode());
        model.addAttribute("users", new User());

        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("users") User user, Model model) {
        try{
            userService.registerUser(user);
            return "redirect:/users/login";

        } catch (UserAlreadyExistsException | LocalDateException |
                 TelephoneException | SpecialSymbolsException | SwearWordsException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("telephone_codes", telephoneCodeRepository.findAll());
            model.addAttribute("telephone_code", new TelephoneCode());
            model.addAttribute("user", user);

            return "registration";
        }
    }

    @GetMapping("/login")
    public String loginUser(Model model) {
        model.addAttribute("users", new User());
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@ModelAttribute("users") User user, Model model, HttpSession session, HttpServletResponse response) {
        try {
            userService.loginUser(user);

            //TODO userRepository in userService
            User oldUser = userRepository.findByLogin(user.getLogin()).get(0);

            //TODO new function
            if (oldUser == null) {
                model.addAttribute("error", "User not found");
                model.addAttribute("user", user);
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Long userId = oldUser.getId();
            session.setAttribute("userId", userId);

            String jsessionId = session.getId();
            Cookie jsessionCookie = new Cookie("JSESSIONID", jsessionId);
            jsessionCookie.setPath("/");
            jsessionCookie.setHttpOnly(true);
            jsessionCookie.setMaxAge(60 * 60 * 3);

            response.addCookie(jsessionCookie);
            response.sendRedirect("/posts");

            return new ResponseEntity<>("Login successful", HttpStatus.OK);

        } catch (GeneralSecurityException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/confirmEmail")
    public String confirmEmail(Model model) {
        model.addAttribute("confirmEmail", new PersonalData());
        return "confirmEmail";
    }

    @PostMapping("/confirmEmail")
    public String confirmEmail(@ModelAttribute("confirmEmail") PersonalData personalData, Model model) {
        try {
            mailService.sendMessage(personalData);
            return "confirmEmailWaiting";
        } catch (LoginException e) {
            model.addAttribute("error", e.getMessage());
            return "confirmEmail";
        }
    }

    @GetMapping("/changePassword/{email}")
    public String changePassword(Model model) {
        model.addAttribute("user", new User());
        return "changePassword";
    }

    @PostMapping("/changePassword/{email}")
    public String changePassword(@PathVariable String email, @ModelAttribute("resetPassword") User user) {
        userService.resetPassword(email, user);
        return "redirect:/users/userProfile";
    }
}
