package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.ReportMessage;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.LoginException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportMessageRepository;
import com.example.demo.repository.UserDataRepository;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

@Controller
@AllArgsConstructor
public class ReportController {

    private final UserRepository userRepository;
    private final ReportMessageRepository reportMessageRepository;
    private final PostRepository postRepository;
    private final UserDataRepository userDataRepository;

    @GetMapping("/report/{entity}/{entityId}/{userId}")
    public String reportMessage(Model model, @PathVariable String entity, @PathVariable Long entityId, @PathVariable Long userId, HttpSession session) {
            if(entity.equals("user") || entity.equals("post") || entity.equals("comment")) {

                model.addAttribute("user_id", userId);
                model.addAttribute("entity", entity);
                model.addAttribute("entityId", entityId);
                model.addAttribute("reportMessage", new ReportMessage());

                return "reportPage";

            } else {
                throw new LoginException("You cant report this entity");
            }
    }

    @PostMapping("/report/{entity}/{entityId}/{userId}")
    public String reportMessage(@ModelAttribute ReportMessage reportMessage, @PathVariable String entity, @PathVariable Long entityId, @PathVariable Long userId, HttpSession session) {
        reportMessage.setEntityId(entityId);
        reportMessage.setEntity(entity);
        reportMessage.setUserId(userId);
        reportMessageRepository.save(reportMessage);
        return "redirect:/posts";
    }

    @GetMapping("/reports")
    public String getAllReportMessages(Model model, HttpSession session) {
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();
        if(user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.OWNER)) {
            model.addAttribute("reportMessages", reportMessageRepository.findAll());
            model.addAttribute("admin", user);
            return "reportMessagesPage";
        } else {
            throw new LoginException("You are not allowed to be here");
        }
    }

    @GetMapping("/reportMessage{id}/{entity}/{entityId}/{userId}")
    public String getReportMessage(Model model, @PathVariable String entity, @PathVariable Long entityId, @PathVariable Long id, @PathVariable Long userId, HttpSession session) {
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();
        model.addAttribute("entity", entity);
        model.addAttribute("admin", user);
        model.addAttribute("entityId", entityId);
        model.addAttribute("reportMessageId", id);
        if(user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.OWNER)) {
            switch (entity) {
                case "user":
                    model.addAttribute("user", userRepository.findById(entityId).get());
                    model.addAttribute("post", null);
                    model.addAttribute("reportMessage", reportMessageRepository.findById(id).get());
                    break;
                case "post":
                    Post post = postRepository.findById(entityId).get();
                    model.addAttribute("user", userRepository.findById(post.getUserId()).isPresent() ? userRepository.findById(post.getUserId()).get() : null);
                    model.addAttribute("post", post);
                    model.addAttribute("reportMessage", reportMessageRepository.findById(id).get());
                    break;
            }
        } else {
            throw new LoginException("You are not allowed to be here");
        }
        return "reportMessage";
    }



    @PostMapping("/reportMessage{id}/{entity}/{entityId}")
    public String deleteReportMessage(HttpSession session, @PathVariable Long entityId, @PathVariable Long id,
                                      @RequestParam(name = "sol1", required = false) String[] sol1,
                                      @RequestParam(name = "sol2", required = false) String[] sol2,
                                      @RequestParam(name = "sol3", required = false) String[] sol3) {
        Long userId = (Long) session.getAttribute("userId");
        if (sol1 != null && sol1.length > 0) {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.getPersonalData().setDescription("");
                userRepository.save(user);
            }
        }
        if (sol2 != null && sol2.length > 0) {
            postRepository.deleteById(entityId);
        }
        if (sol3 != null && sol3.length > 0) {
            userRepository.findById(id).ifPresent(user -> {
                reportMessageRepository.delete(reportMessageRepository.findByUserId(user.getId()).get());
                userDataRepository.delete(user.getPersonalData());
                postRepository.deleteAllByUserId(user.getId());
                userRepository.delete(user);
            });
        }
        reportMessageRepository.deleteById(id);
        return "redirect:/reports";
    }

}
