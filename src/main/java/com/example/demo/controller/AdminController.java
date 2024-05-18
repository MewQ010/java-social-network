package com.example.demo.controller;

import com.example.demo.entity.Post;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ReportMessageRepository reportMessageRepository;
    private final UserDataRepository userDataRepository;
    private final PostRepository postRepository;

    @GetMapping("/UserToAdmin{id}")
    public String makeAdmin(@PathVariable Long id, HttpSession session) {
        User admin = userRepository.findById((Long) session.getAttribute("userId")).get();
        if(admin.getRole().equals(UserRole.OWNER)) {
            User user = userRepository.findById(id).get();
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
            return "redirect:/posts";
        } else {
            throw new LoginException("Bro you are not Owner to set Admin status");
        }
    }

    @GetMapping("/deleteUser{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) throws javax.security.auth.login.LoginException {
        User admin = userRepository.findById((Long) session.getAttribute("userId")).get();
        if(admin.getRole().equals(UserRole.ADMIN) || admin.getRole().equals(UserRole.OWNER)) {
            User user = userRepository.findById(id).get();
            reportMessageRepository.delete(reportMessageRepository.findByUserId(user.getId()).get());
            userDataRepository.delete(user.getPersonalData());
            postRepository.deleteAllByUserId(user.getId());
            userRepository.delete(user);
            return "redirect:/reports";
        } else {
            throw new javax.security.auth.login.LoginException("Bro you are not allowed to delete Users");
        }
    }

    @GetMapping("/clearDescription{id}")
    public String clearDescription(@PathVariable Long id, HttpSession session) throws javax.security.auth.login.LoginException {
        User admin = userRepository.findById((Long) session.getAttribute("userId")).get();
        if(admin.getRole().equals(UserRole.ADMIN) || admin.getRole().equals(UserRole.OWNER)) {
            User user = userRepository.findById(id).get();
            user.getPersonalData().setDescription("");
            userRepository.save(user);
            return "redirect:/reports";
        } else {
            throw new javax.security.auth.login.LoginException("Bro you are not allowed to clear Descriptions");
        }
    }

    @GetMapping("/deletePost{id}")
    public String deletePost(@PathVariable Long id, HttpSession session) throws javax.security.auth.login.LoginException {
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();
        if(user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.OWNER)) {
            Post post = postRepository.findById(id).get();
            reportMessageRepository.delete(reportMessageRepository.findByUserId(post.getUserId()).isPresent()? reportMessageRepository.findByUserId(post.getUserId()).get() : null);
            postRepository.delete(postRepository.findById(id).isPresent()? postRepository.findById(id).get() : null);
            return "redirect:/reports";
        } else {
            throw new javax.security.auth.login.LoginException("Bro you are not allowed to delete Posts");
        }
    }
}
