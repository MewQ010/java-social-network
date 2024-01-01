package com.example.demo.controller;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.User;
import com.example.demo.repository.UserDataRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.passay.PasswordData;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.login.LoginException;


@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String getUsers(Model model) {
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("user", new User());
        return "index";
    }

    @GetMapping("/register")
    public String createUser(User user, Model model) {
        model.addAttribute("users", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("users") User user) {
        userService.registerUser(user);
        return "redirect:/users";
    }

    @GetMapping("/login")
    public String loginUser(User user, Model model) {
        model.addAttribute("users", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("users") User user) throws LoginException {
        userService.loginUser(user);
        return "redirect:/users";
    }

    @GetMapping("/confirmEmail")
    public String changePassword(Model model, PersonalData personalData) {
        model.addAttribute("confirmEmail", new PersonalData());

        return "confirmEmail";
    }

    @PostMapping("/confirmEmail")
    public String changePassword(@ModelAttribute("confirmEmail") PasswordData personalData) {

        return "resetPassword";
    }

}
