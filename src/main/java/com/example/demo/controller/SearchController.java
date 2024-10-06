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
    public String searchUsers(@RequestParam(value = "query", required = false) String query, @RequestParam(value = "page", defaultValue = "1") Integer page, Model model) {
        if(query != null) {
            List<User> users = userService.searchUsers(query);
            List<User> posts = Lists.reverse(users);

            int pageSize = 5;
            int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

            if (page == null || page < 1 || page > totalPages) {
                page = 1;
            }

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, posts.size());

            List<User> currentPagePosts = (posts.size() > 0 && startIndex < posts.size())
                    ? posts.subList(startIndex, endIndex)
                    : new ArrayList<>();
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("posts", currentPagePosts);
            model.addAttribute("users", users); //TODO use DTO only
        }
        model.addAttribute("query", query);
        return "search_form";
    }

    @GetMapping("/search-fragment")
    public String searchUsersFragment(@RequestParam(value = "q", required = false) String query, Model model, @RequestParam(value = "page", defaultValue = "1") Integer page) throws IOException {
        if(query != null && !query.isEmpty()) {
            List<User> users = userService.searchUsers(query);
            List<User> posts = Lists.reverse(users);

            int pageSize = 5;
            int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

            if (page == null || page < 1 || page > totalPages) {
                page = 1;
            }

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, posts.size());

            List<User> currentPagePosts = (posts.size() > 0 && startIndex < posts.size())
                    ? posts.subList(startIndex, endIndex)
                    : new ArrayList<>();
            List<String> usersProfileImage = new ArrayList<>();
            String base64Image;

            for(User user : users) {
                usersProfileImage.add(base64Image = awsService.getImageFromAWS(user.getPersonalData().getProfileImageUrl()));
            }

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("posts", currentPagePosts);
            model.addAttribute("usersProfileImage", usersProfileImage);
            model.addAttribute("users", users);
        }
        return "fragments/search-results :: resultsList";
    }

    @PostMapping("/search")
    public String searchUser(@RequestParam String name, Model model, HttpSession session) {
        List<User> listOfUsers = userRepository.findAll();
        List<String> listOfUsersLogin = new ArrayList<>();

        for(User user : listOfUsers) {
            listOfUsersLogin.add(user.getLogin());
        }

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
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();
        model.addAttribute("user", user);
        model.addAttribute("listOfUsersLogin", listOfUsersLogin);
        model.addAttribute("ListOfUsers", listOfUsers);
        return "search_result";
    }

    @GetMapping("/profile{userName}")
    public String showProfile(Model model, @PathVariable String userName, HttpSession session, Integer page) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findByLogin(userName).get(0);

        List<Post> posts = Lists.reverse(postRepository.findAllByUserId(user.getId()));
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLiked = new ArrayList<>();

        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;

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
            isLiked.add(post.getLikeList().contains(userId));
        }

        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }

        model.addAttribute("currentPostImages", currentPostImages);
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
