package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomePageService {

    private final PostRepository postRepository;

    public Page<Post> getAllPosts(Pageable pageable){
        return postRepository.findAll(pageable);
    }
    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }

}
