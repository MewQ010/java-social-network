package com.example.demo.controller;

import com.example.demo.entity.CV;
import com.example.demo.entity.PersonalData;
import com.example.demo.entity.User;
import com.example.demo.repository.CVRepository;
import com.example.demo.repository.UserDataRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AWSService;
import com.example.demo.service.CVService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@AllArgsConstructor
@Controller
@RequestMapping("/cv")
public class CVController {
    
    private final UserRepository userRepository;
    private final CVRepository cvRepository;
    private final AWSService awsService;
    private final CVService cvService;

    @GetMapping("/{login}")
    public String getCV(Model model, @PathVariable("login") String login, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        User cv_user = userRepository.findByLogin(login).get(0);
        CV cv = cv_user.getCv();
        String image_url = cv.getImage_url();
        String image;
        if(image_url != null) {
            image = awsService.getImageFromAWS(image_url);
        } else {
            image = null;
        }
        model.addAttribute("cv_user", cv_user);
        model.addAttribute("cv", cv);
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        model.addAttribute("image", image);

        return "getCV";
    }

    @GetMapping("/edit")
    public String editCV(Model model, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        CV cv = user.getCv();
        String image = null;
        if(cv.getImage_url() != null) {
            image = awsService.getImageFromAWS(cv.getImage_url());
        }

        model.addAttribute("image", image);
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        model.addAttribute("cv", cv);

        return "editCV";
    }

    @PostMapping("/edit")
    public String editCV(@RequestParam(value = "file", required = false) MultipartFile multipartFile, HttpSession session, @ModelAttribute CV cv) throws LoginException, IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        PersonalData personalData = user.getPersonalData();
        CV oldCV = user.getCv();
        if(!multipartFile.isEmpty()) {
            String savedImage = awsService.saveImageToAWS(multipartFile, user.getLogin());
            oldCV.setImage_url(savedImage);
        } else {
            oldCV.setImage_url(null);
        }

        oldCV.setFirstName(personalData.getFirstName());
        oldCV.setLastName(personalData.getLastName());
        oldCV.setDescription(cv.getDescription());
        oldCV.setGithub_link(cv.getGithub_link());
        oldCV.setInstagram_link(cv.getInstagram_link());
        oldCV.setTelegram_link(cv.getTelegram_link());

        cvRepository.save(oldCV);


        return "redirect:/posts";
    }

    @GetMapping("/delete-cv")
    public String deleteCV(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).get();
        cvService.deleteCV(user.getCv());
        return "redirect:/users/userProfile";
    }

}
