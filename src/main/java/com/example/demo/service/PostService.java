package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> searchPosts(String keyword) {
        return postRepository.findByHeaderContaining(keyword);
    }

}
