package com.example.demo.controller;

import com.example.demo.entity.*;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AWSService awsService;
    private final PostRepository postRepository;
    private final TelephoneCodeRepository telephoneCodeRepository;
    private final UserRepository userRepository;

    @GetMapping("/userProfile")
    public String userProfile(Model model, HttpSession session, Integer page) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;

        List<Post> posts = Lists.reverse(postRepository.findAllByUserId(userId));
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLikedList = new ArrayList<>();

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

        if (page == null || page < 1 || page > totalPages) {
            page = 1;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, posts.size());
        List<Post> currentPagePosts = posts.subList(startIndex, endIndex);

        List<String> currentPostImages = new ArrayList<>();
        for(Post post : currentPagePosts) {
            if(post.getPostImage() != null) {
                base64Image = awsService.getImageFromAWS(post.getPostImage());
                currentPostImages.add(base64Image);
            } else {
                currentPostImages.add("");
            }
        }

        for(Post post : currentPagePosts) {
            likesList.add(post.getLikeList().size());
            isLikedList.add(post.getLikeList().contains(userId));
        }

        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }

        model.addAttribute("currentPostImages", currentPostImages);
        model.addAttribute("isLiked", isLikedList);
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
    public String editUser(Model model, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        String image = awsService.getImageFromAWS(user.getPersonalData().getProfileImageUrl());

        model.addAttribute("user", user);
        model.addAttribute("personal_data", user.getPersonalData());
        model.addAttribute("telephone_codes", telephoneCodeRepository.findAll());
        model.addAttribute("telephone_code", new TelephoneCode());
        model.addAttribute("dateOfBirth", user.getPersonalData().getDateOfBirth());
        model.addAttribute("image",image);

        return "editUser";
    }

    @PostMapping("/edit")
    public String editUser(@ModelAttribute("personalData") PersonalData personalData, HttpSession session,
                           @RequestParam("file")MultipartFile multipartFile) throws LoginException, IOException {

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
