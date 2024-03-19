package com.example.demo.controller;

import com.example.demo.entity.Message;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AWSService;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Controller
public class MessageController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final AWSService awsService;


    @GetMapping("/")
    public String index() {
        return "lobby";
    }

    @GetMapping("/profile{userName}")
    public String showProfile(Model model, @PathVariable String userName, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findByLogin(userName).get(0);

        List<Post> userPosts = postRepository.findAllByUserId(user.getId());
        model.addAttribute("userPosts", Lists.reverse(userPosts));
        model.addAttribute("user", user);
        model.addAttribute("name", userRepository.findById(userId).get().getLogin());

        String imageKey = user.getPersonalData().getProfileImageUrl();
        String base64Image;
        try {
            base64Image = awsService.getImageFromAWS(imageKey);
        } catch (IOException e) {
            base64Image = "def-profile-img.jpg";
        }

        model.addAttribute("base64Image", base64Image);

        return "profile";
    }

    @GetMapping("/chat/{receiver}")
    public String chat(HttpSession session, @PathVariable String receiver, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String sender = userRepository.findById(userId).get().getLogin();
        List<Message> messages = messageRepository.findBySenderAndReceiverOrSenderAndReceiverOrderByTimestampAsc(sender, receiver, receiver, sender);
        model.addAttribute("messages", messages);
        model.addAttribute("sender", sender);
        model.addAttribute("receiver", receiver);
        return "chat";
    }

    @PostMapping("/send")
    public String send(HttpSession session, @RequestParam String receiver, @RequestParam String content) {
        Long userId = (Long) session.getAttribute("userId");
        String sender = userRepository.findById(userId).get().getLogin();
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        messageRepository.save(message);
        return "redirect:/chat/" + receiver;
    }

    @GetMapping("/chat")
    public String getUserChats(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String login = userRepository.findById(userId).get().getLogin();
        List<Message> messagesList = messageRepository.findBySenderOrReceiver(login, login);
        List<String> profileImages = new ArrayList<>();
        Set<User> usersSet = new HashSet<>();
        for(Message i : messagesList) {
            if(!i.getSender().equals(userRepository.findById(userId).get().getLogin())) {
                usersSet.add(userRepository.findByLogin(i.getSender()).get(0));
            } else {
                usersSet.add(userRepository.findByLogin(i.getReceiver()).get(0));
            }
        }
        String base64Image;
        String imageKey;
        model.addAttribute("userChat", usersSet);
        for(User user : usersSet) {
            imageKey = user.getPersonalData().getProfileImageUrl();
            try {
                base64Image = awsService.getImageFromAWS(imageKey);
                profileImages.add(base64Image);
            } catch (IOException e) {
                base64Image = "def-profile-img.jpg";
            }
        }
        model.addAttribute("base64Images", profileImages);
        return "userChatPage";
    }
}