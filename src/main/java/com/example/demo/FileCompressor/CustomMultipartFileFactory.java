package com.example.demo.FileCompressor;

import org.springframework.stereotype.Component;

@Component
public class CustomMultipartFileFactory {
    public CustomMultipartFile create(byte[] content, String name, String originalFilename, String contentType) {
        return new CustomMultipartFile(content, name, originalFilename, contentType);
    }
}
