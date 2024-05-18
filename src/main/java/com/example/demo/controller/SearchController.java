package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.LoginException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AWSService;
import com.example.demo.service.UserService;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {

    private final AWSService awsService;
    private final PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @GetMapping("/search")
    public String showSearchForm() {
        return "search_form";
    }

    @PostMapping("/search")
    public String searchUser(@RequestParam String name, Model model) {

        try {
            List<User> users = userRepository.findByLogin(name);
            model.addAttribute("users", users);
            String imageKey = users.get(0).getPersonalData().getProfileImageUrl();

            String base64Image;

            try {
                base64Image = awsService.getImageFromAWS(imageKey);
            } catch (IOException e) {
                base64Image = "def-profile-img.jpg";
            }

            model.addAttribute("base64Image", base64Image);

        } catch (IndexOutOfBoundsException e) {

                model.addAttribute("error", "Sorry we can not find User with this Login " + name);
        }
        return "search_result";
    }

    @GetMapping("/profile{userName}")
    public String showProfile(Model model, @PathVariable String userName, HttpSession session, Integer page) {

        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findByLogin(userName).get(0);

        List<Post> posts = Lists.reverse(postRepository.findAllByUserId(user.getId()));
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLiked = new ArrayList<>();

        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;
        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }

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
        model.addAttribute("userId", userRepository.findById(userId).get().getId());
        model.addAttribute("name", userRepository.findById(userId).get().getLogin());
        model.addAttribute("role", user.getRole().equals(UserRole.OWNER));

        return "profile";
    }
}
