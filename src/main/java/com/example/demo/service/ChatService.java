package com.example.demo.service;

import com.example.demo.entity.Chat;
import com.example.demo.entity.Message;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public void sendMessage(String sender, String receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        messageRepository.save(message);
    }

    public void createChat(String sender, String receiver) {
        if(chatRepository.findBySenderAndReceiverOrSenderAndReceiver(sender, receiver, receiver, sender).isEmpty()) {
            String randomUUIDString = UUID.randomUUID().toString();
            Chat chat = new Chat();
            chat.setSender(sender);
            chat.setReceiver(receiver);
            chat.setChannel(randomUUIDString.replaceAll("-", "").substring(0, 7));
            chatRepository.save(chat);
        }
    }
}
