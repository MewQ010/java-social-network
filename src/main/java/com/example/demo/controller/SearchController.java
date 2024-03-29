package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AWSService;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {

    private final AWSService awsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @GetMapping("/search")
    public String showSearchForm() {
        return "search_form";
    }

    @PostMapping("/search")
    public String searchUser(@RequestParam String name, Model model) throws IOException {
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
}
