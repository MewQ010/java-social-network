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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
    private final ReportMessageRepository reportMessageRepository;

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
    public String loginUser(@ModelAttribute("users") User user, HttpSession session, Model model) {
        try {
            userService.loginUser(user);
            User oldUser = userRepository.findByLogin(user.getLogin()).get(0);

            if (oldUser == null) {
                model.addAttribute("error", "User not found");
                model.addAttribute("user", user);
                return "login";
            }

            session.setAttribute("userId", oldUser.getId());
            return "redirect:/posts";

        } catch (GeneralSecurityException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
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
    public String userProfile(Model model, HttpSession session, Integer page) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;

        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }

        List<Post> posts = Lists.reverse(postRepository.findAllByUserId(userId));
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLiked = new ArrayList<>();

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

        if (page == null || page < 1 || page > totalPages) {
            page = 1;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, posts.size());
        List<Post> currentPagePosts = posts.subList(startIndex, endIndex);

        for(Post post : currentPagePosts) {
            likesList.add(post.getLikeList().size());
            isLiked.add(post.getLikeList().contains(userId));
        }

        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesList", likesList);
        model.addAttribute("base64Image", base64Image);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("posts", currentPagePosts);
        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.OWNER));

        return "userProfile";
    }

    @GetMapping("/editUser")
    public String editUser(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();

        model.addAttribute("user", user);
        model.addAttribute("personal_data", user.getPersonalData());
        model.addAttribute("telephone_codes", telephoneCodeRepository.findAll());
        model.addAttribute("telephone_code", new TelephoneCode());
        model.addAttribute("dateOfBirth", user.getPersonalData().getDateOfBirth());

        return "editUser";
    }

    @PostMapping("/editUser")
    public String editUser(@ModelAttribute("personalData") PersonalData personalData, HttpSession session,
                           @RequestParam("file")MultipartFile multipartFile) throws LoginException {

        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();

        if(!multipartFile.isEmpty()) {
            String profileImage = awsService.saveImageToAWS(multipartFile, user.getLogin());
            personalData.setProfileImageUrl(profileImage);
            PersonalData oldPersonalData = userRepository.findById(userId).get().getPersonalData();
            userService.savePersonalData(oldPersonalData ,personalData, profileImage);

        } else {
            PersonalData oldPersonalData = userRepository.findById(userId).get().getPersonalData();
            userService.savePersonalData(oldPersonalData ,personalData);
        }

        return "redirect:/users/userProfile";
    }

    @GetMapping("/logout")
    public String logOutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }

}
