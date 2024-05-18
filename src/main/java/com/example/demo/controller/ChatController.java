package com.example.demo.controller;

import com.example.demo.entity.Chat;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.exception.LoginException;
import com.example.demo.repository.*;
import com.example.demo.service.AWSService;
import com.example.demo.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@AllArgsConstructor
@Controller
public class ChatController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AWSService awsService;
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/chat/{receiver}")
    public String chat(HttpSession session, @PathVariable String receiver, Model model) throws IOException {

        if(!userRepository.existsByLogin(receiver)) {
            throw new LoginException("We cant find User with this Login " + receiver);
        }

        Long userId = (Long) session.getAttribute("userId");
        String sender = userRepository.findById(userId).get().getLogin();
        chatService.createChat(sender, receiver);
        String channel = chatRepository.findBySenderAndReceiverOrSenderAndReceiver(sender, receiver, receiver, sender).get(0).getChannel();
        model.addAttribute("messages", messageRepository.findByChannel(channel));
        model.addAttribute("sender", sender);
        model.addAttribute("user", userRepository.findById(userId).get());
        model.addAttribute("receiver", receiver);
        model.addAttribute("channel", channel);

        return "chat";
    }

    @PostMapping("/send")
    public void send(HttpSession session, @RequestParam String receiver, @RequestParam String content) {
        Long userId = (Long) session.getAttribute("userId");
        chatService.sendMessage(userRepository.findById(userId).get().getLogin(), receiver, content);
    }

    @GetMapping("/chat")
    public String getUserChats(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String login = userRepository.findById(userId).get().getLogin();

        List<Chat> chatsList = chatRepository.findBySenderOrReceiver(login, login);
        List<String> profileImages = new ArrayList<>();
        Set<User> usersSet = new HashSet<>();

        for(Chat i : chatsList) {
            if(!i.getSender().equals(login)) {
                usersSet.add(userRepository.findByLogin(i.getSender()).get(0));
            } else {
                usersSet.add(userRepository.findByLogin(i.getReceiver()).get(0));
            }
        }
        usersSet.remove(userRepository.findById(userId).get());

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
                profileImages.add(base64Image);
            }
        }
        model.addAttribute("base64Images", profileImages);

        return "userChatPage";
    }

    @GetMapping("/videoCall/{receiver}/{channel}")
    public String videoCall(Model model, @PathVariable String receiver, @PathVariable String channel, HttpSession session) {
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();

        model.addAttribute("user", user);
        model.addAttribute("channel", channel);
        model.addAttribute("receiver", receiver);

        return "videoCall";
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        var message = Message.builder()
                .sender(chatMessage.getSender())
                .channel(chatMessage.getRecipient())
                .content(chatMessage.getContent())
                .build();
        messageRepository.save(message);
        messagingTemplate.convertAndSendToUser(chatMessage.getRecipient(), "/queue/messages", chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    }

}
