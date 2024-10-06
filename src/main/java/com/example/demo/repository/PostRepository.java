package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long id);
    void deleteAllByUserId(Long id);
    List<Post> findByHeaderContaining(String login);
    List<Post> findByHeaderStartingWithIgnoreCase(String login);
}
