package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.repository.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CVService {
    private final CVRepository cvRepository;

    public void deleteCV(CV cv) {
        cv.setImage_url(null);
        cv.setInstagram_link("");
        cv.setTelegram_link("");
        cv.setGithub_link("");
        cv.setDescription("This user doesn't made his cv yet");
        cvRepository.save(cv);
    }
}
