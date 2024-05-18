package com.example.demo.controller;

import com.example.demo.repository.*;
import com.example.demo.service.AWSService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class HomePageController {

    private final AWSService awsService;
    private final UserRepository userRepository;
    @GetMapping("/")
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
