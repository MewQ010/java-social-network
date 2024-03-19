package com.example.demo.controller;

import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/home")
public class HomePageController {

//    private final HomePageService mainService;
    private final UserRepository userRepository;

//    @GetMapping("/posts")
//    public ResponseEntity<Page<Post>> getAllPosts(@RequestParam(defaultValue = "1") int page,
//                                                  @RequestParam(defaultValue = "10") int size) {
//        Pageable pageable = PageRequest.of(page - 1, size);
//        Page<Post> posts = mainService.getAllPosts(pageable);
//        return ResponseEntity.ok(posts);
//    }

    @GetMapping
    public String mainPage(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) {
            model.addAttribute("userId", false);
        } else {
            model.addAttribute("userId", true);
            model.addAttribute("user", userRepository.findById(userId).get());
        }
        return "mainPage";
    }

}
