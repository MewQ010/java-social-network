package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import com.example.demo.service.HomePageService;
import com.example.demo.service.UserService;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;
    private final HomePageService homePageService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @GetMapping
    public String postView(Model model, HttpSession session, Integer page) {

        Long id = (Long) session.getAttribute("userId");
        session.setAttribute("userId", id);
        List<Post> allPosts = homePageService.getAllPosts();
        List<String> userNames = new ArrayList<>();

        for (Post post : allPosts) {
            Optional<User> user = userRepository.findById(post.getUserId());
            userNames.add(user.get().getLogin());
        }
        userNames = Lists.reverse(userNames);
        List<Post> posts = Lists.reverse(postRepository.findAll());

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

        if (page == null || page < 1 || page > totalPages) {
            page = 1;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, posts.size());
        List<Post> currentPagePosts = posts.subList(startIndex, endIndex);
        userNames = userNames.subList(startIndex, endIndex);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("posts", currentPagePosts);
        model.addAttribute("userNames", userNames);
        model.addAttribute("login", userRepository.findById(id).get());


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
        post.setUserId(userId);
        userService.addPost(post);

        return "redirect:/posts";
    }

    @PostMapping("/delete")
    public String deletePost(@RequestParam Long postId) {

        Post post = postRepository.findById(postId).isPresent() ? postRepository.findById(postId).get() : null;
        postRepository.delete(Objects.requireNonNull(post));

        return "redirect:/users/userProfile";
    }

    @GetMapping("/comments/{postId}")
    public String getPostComments(HttpSession session, Model model, @PathVariable Long postId) {

        User user = userRepository.findById((Long) session.getAttribute("userId")).get();

        model.addAttribute("comments", commentRepository.findByPostId(postId));
        model.addAttribute("user", user);
        model.addAttribute("post", postRepository.findById(postId).get());

        return "postComments";
    }

    @PostMapping("/send")
    public String send(HttpSession session, @RequestParam String content, @RequestParam Long postId) {

        Long userId = (Long) session.getAttribute("userId");
        String sender = userRepository.findById(userId).get().getLogin();

        Comment comment = new Comment();
        comment.setSender(sender);
        comment.setPostId(postId);
        comment.setComment(content);
        commentRepository.save(comment);

        return "redirect:/posts/comments/" + postId;
    }

    @GetMapping("/edit")
    public String editPost(Model model, @RequestParam Long postId) {

        model.addAttribute("post", postRepository.findById(postId));

        return "postEdit";
    }

    @PostMapping("/edit")
    public String editPost(@ModelAttribute Post post, @RequestParam Long postId) {

        postRepository.save(post);

        return "redirect:/users/userProfile";
    }

}
