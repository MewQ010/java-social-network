package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.ReportMessage;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.LoginException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportMessageRepository;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class ReportController {

    private final UserRepository userRepository;
    private final ReportMessageRepository reportMessageRepository;
    private final PostRepository postRepository;

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
            if(user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.OWNER)) {
                switch (entity) {

                    case ("user"):
                        model.addAttribute("user", userRepository.findById(entityId).get());
                        model.addAttribute("post", null);
                        model.addAttribute("reportMessage", reportMessageRepository.findById(id).get());
                        break;

                    case ("post"):
                        Post post = postRepository.findById(entityId).get();
                        model.addAttribute("user", userRepository.findById(post.getUserId()).isPresent() ? userRepository.findById(post.getUserId()).get() : null);
                        model.addAttribute("post", postRepository.findById(entityId).get());
                        model.addAttribute("reportMessage", reportMessageRepository.findById(id).get());
                        break;

                }
            } else {
                throw new LoginException("You are not allowed to be here");
            }
            return "reportMessage";
    }

    @PostMapping("/reportMessage{id}/{entity}/{entityId}")
    public String deleteReportMessage(@PathVariable Long id) {
        reportMessageRepository.delete(reportMessageRepository.findById(id).get());
        return "redirect:/reports";
    }
}
