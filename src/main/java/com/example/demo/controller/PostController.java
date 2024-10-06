package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.LoginException;
import com.example.demo.repository.*;
import com.example.demo.service.AWSService;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AWSService awsService;

    @PostMapping("/search")
    public String searchPosts(@RequestParam(value = "q", required = false) String query) {
        return "redirect:/posts" + query;
    }

    @GetMapping
    public String postView(@RequestParam(value = "q", required = false) String query, Model model, HttpSession session, @RequestParam(value = "page", defaultValue = "1") Integer page) throws IOException {

        List<Post> allPosts;

        if(query == null) {
            allPosts = postRepository.findAll();
        } else {
            allPosts = postService.searchPosts(query);
        }

        model.addAttribute("users", allPosts);
        model.addAttribute("query", query);

        Long id = (Long) session.getAttribute("userId");
        List<String> userNames = new ArrayList<>();
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLiked = new ArrayList<>();

        for (Post post : allPosts) {
            Optional<User> user = userRepository.findById(post.getUserId());
            if (user.isPresent()) {
                userNames.add(user.get().getLogin());
            }
        }

        userNames = Lists.reverse(userNames);
        List<Post> posts = Lists.reverse(allPosts);

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

        if (page == null || page < 1 || page > totalPages) {
            page = 1;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, posts.size());

        List<Post> currentPagePosts = (posts.size() > 0 && startIndex < posts.size())
                ? posts.subList(startIndex, endIndex)
                : new ArrayList<>();

        List<String> currentPostImages = new ArrayList<>();
        for (Post post : currentPagePosts) {
            if (post.getPostImage() != null) {
                String base64Image = awsService.getImageFromAWS(post.getPostImage());
                currentPostImages.add(base64Image);
            } else {
                currentPostImages.add("");
            }
        }

        userNames = userNames.subList(Math.min(startIndex, userNames.size()), Math.min(endIndex, userNames.size()));

        for (Post post : currentPagePosts) {
            likesList.add(post.getLikeList().size());
            isLiked.add(post.getLikeList().contains(id));
        }

        model.addAttribute("currentPostImages", currentPostImages);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesList", likesList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("posts", currentPagePosts);
        model.addAttribute("userNames", userNames);
        model.addAttribute("login", userRepository.findById(id).orElse(null));
        model.addAttribute("post", new Post());

        return "postView";
    }

    @GetMapping("/search-post-fragment")
    public String searchPostsFragment(@RequestParam(value = "q", required = false) String query, Model model, @RequestParam(value = "page", defaultValue = "1") Integer page, HttpSession session) throws IOException {

        List<Post> allPosts;

        if(query == null) {
            allPosts = postRepository.findAll();
        } else {
            allPosts = postService.searchPosts(query);
        }

        model.addAttribute("users", allPosts);
        model.addAttribute("query", query);

        Long id = (Long) session.getAttribute("userId");
        List<String> userNames = new ArrayList<>();
        List<Integer> likesList = new ArrayList<>();
        List<Boolean> isLiked = new ArrayList<>();

        for (Post post : allPosts) {
            Optional<User> user = userRepository.findById(post.getUserId());
            if (user.isPresent()) {
                userNames.add(user.get().getLogin());
            }
        }

        userNames = Lists.reverse(userNames);
        List<Post> posts = Lists.reverse(allPosts);

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) posts.size() / pageSize);

        if (page == null || page < 1 || page > totalPages) {
            page = 1;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, posts.size());

        List<Post> currentPagePosts = (posts.size() > 0 && startIndex < posts.size())
                ? posts.subList(startIndex, endIndex)
                : new ArrayList<>();

        List<String> currentPostImages = new ArrayList<>();
        for (Post post : currentPagePosts) {
            if (post.getPostImage() != null) {
                String base64Image = awsService.getImageFromAWS(post.getPostImage());
                currentPostImages.add(base64Image);
            } else {
                currentPostImages.add("");
            }
        }

        userNames = userNames.subList(Math.min(startIndex, userNames.size()), Math.min(endIndex, userNames.size()));

        for (Post post : currentPagePosts) {
            likesList.add(post.getLikeList().size());
            isLiked.add(post.getLikeList().contains(id));
        }

        model.addAttribute("currentPostImages", currentPostImages);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesList", likesList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("posts", currentPagePosts);
        model.addAttribute("userNames", userNames);
        model.addAttribute("login", userRepository.findById(id).orElse(null));
        model.addAttribute("post", new Post());

        return "fragments/post-results :: resultsList";
    }

    @GetMapping("/add")
    public String addPost(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        model.addAttribute("userId", userId);
        model.addAttribute("post", new Post());

        return "postAddPage";
    }

    @PostMapping("/add")
    public String addPost(@ModelAttribute("post") Post post, @RequestParam(value = "file", required = false) MultipartFile multipartFile, HttpSession session) throws IOException, javax.security.auth.login.LoginException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        if(multipartFile != null && !multipartFile.isEmpty()) {
            String profileImage = awsService.saveImageToAWS(multipartFile, "PostImage" + user.getLogin());
            post.setPostImage(profileImage);
        }
        post.setUserId(userId);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
        post.setTime(time);
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

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody Post post, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Post savedPost = postRepository.save(post);
            return ResponseEntity.ok(savedPost);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
    }

    @PostMapping("/unlike/{postId}")
    @ResponseBody
    public void unlikePost(@PathVariable Long postId, HttpSession session) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            Long userId = (Long) session.getAttribute("userId");
            post.getLikeList().remove(userId);
            postRepository.save(post);
        } else {
            throw new LoginException("Wrong, You are invalid");
        }
    }

}
