package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AWSService awsService;
    private final UserDataRepository userDataRepository;
    private final PostRepository postRepository;
    private final TelephoneCodeRepository telephoneCodeRepository;
    private final UserRepository userRepository;
    private final MailService mailService;


    @GetMapping
    public String getUsers(Model model) {
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("user", new User());
        return "index";
    }

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
        } catch (UserAlreadyExistsException | LoginException  | LocalDateException |
                 TelephoneException | SpecialSymbolsException | SwearWordsException e) {
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
    }

    @GetMapping("/login")
    public String loginUser(Model model) {
        model.addAttribute("users", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("users") User user, HttpSession session, Model model) {
        try {
            try {
                userService.loginUser(user);
            } catch (NullPointerException e) {
                model.addAttribute("error", e.getMessage());
                return "login";
            }
            User oldUser = userRepository.findByPersonalData(userDataRepository.findByEmail(user.getPersonalData().getEmail()));
            if (oldUser != null) {
                session.setAttribute("userId", oldUser.getId());
                return "redirect:/posts";
            } else {
                model.addAttribute("error", "User not found");
                return "login";
            }
        } catch (LoginException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
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

    @GetMapping("/userProfile")
    public String userProfile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        List<Post> userPosts = postRepository.findAllByUserId(userId);
        model.addAttribute("userPosts", Lists.reverse(userPosts));
        model.addAttribute("user", user);
        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;
        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }
        model.addAttribute("base64Image", base64Image);
        return "userProfile";
    }

    @GetMapping("/editUser")
    public String editUser(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("user", userRepository.findById(userId).get());
        model.addAttribute("personal_data", userDataRepository.findById(userId));
        model.addAttribute("telephone_codes", telephoneCodeRepository.findAll());
        model.addAttribute("telephone_code", new TelephoneCode());
        return "editUser";
    }

    @PostMapping("/editUser")
    public String editUser(@ModelAttribute("personalData") PersonalData personalData, HttpSession session,
                           @RequestParam("file")MultipartFile multipartFile) throws LoginException, IOException {
        Long userId = (Long) session.getAttribute("userId");
        String profileImage = awsService.saveImageToAWS(multipartFile);
        personalData.setProfileImageUrl(profileImage);
        PersonalData oldPersonalData = userRepository.findById(userId).get().getPersonalData();
        userService.savePersonalData(oldPersonalData ,personalData, profileImage);
        return "redirect:/users/userProfile";
    }

}
