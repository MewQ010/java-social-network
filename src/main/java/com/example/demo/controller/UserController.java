package com.example.demo.controller;

import com.example.demo.entity.PersonalData;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.UserDataRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.HomePageService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;


@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final HomePageService homePageService;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

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
        return "/login";
    }

    @GetMapping("/login")
    public String loginUser(User user, Model model) {
        model.addAttribute("users", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("users") User user, HttpSession session) throws LoginException {
        userService.loginUser(user);
        User oldUser = userRepository.findByPersonalData(userDataRepository.findByEmail(user.getPersonalData().getEmail()));
        session.setAttribute("userId", oldUser.getId());
        return "redirect:/users/posts";
        //        return "/posts";
    }

    @GetMapping("/confirmEmail")
    public String confirmEmail(Model model, PersonalData personalData) {
        model.addAttribute("confirmEmail", new PersonalData());
        return "confirmEmail";
    }

    @PostMapping("/confirmEmail")
    public String confirmEmail(@ModelAttribute("confirmEmail") PersonalData personalData) {
        userService.sendMessage(personalData);

        return "confirmEmailWaiting";
    }

    @GetMapping("/reset{email}")
    public String changePassword(@PathVariable String email, Model model, User user) {
        model.addAttribute("resetPassword", new User());
        return "changePassword";
    }

    @PostMapping("/reset{email}")
    public String changePassword(@PathVariable String email, @ModelAttribute("resetPassword") User user) {
        userService.resetPassword(email, user);
        return "redirect:/users";
    }

    @GetMapping("/posts")
    public String postView(Model model, HttpSession session) {
        model.addAttribute("posts", homePageService.getAllPosts());
        model.addAttribute("post", new Post());
        Long id = (Long) session.getAttribute("userId");
        session.setAttribute("userId", id);
        return "postView";
    }

    @GetMapping("/addPost")
    public String addPost(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        model.addAttribute("userId", userId);
        model.addAttribute("post", new Post());
        return "postAddPage";
    }

    @PostMapping("/addPost")
    public String addPost(@ModelAttribute("post") Post post, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        post.setId(userId);
        userService.addPost(post);
        return "redirect:/users/posts";
    }

}
