package com.example.demo.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
public class AWSService {


    @Autowired
    S3Service s3Service;

    @Autowired
    AmazonS3 amazonS3;

    public boolean doesObjectExist(String bucketName, String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    public String saveImageToAWS(MultipartFile multipartFile) throws LoginException, IOException {
        String profileImage = multipartFile.getOriginalFilename();
        Long counter = 1L;

        while(doesObjectExist("intitajavaproject", profileImage)) {
            profileImage = profileImage + counter;
            counter++;
        }

        try {
            s3Service.uploadToS3(multipartFile.getInputStream(), profileImage);
            return profileImage;
        } catch (IOException | SdkClientException e) {
            throw new LoginException("Error saving your image");
        }
    }

    public String getImageFromAWS(String imageKey) throws IOException {
        S3Object object = amazonS3.getObject("intitajavaproject", imageKey);
        InputStream inputStream = object.getObjectContent();
        byte[] imageBytes = inputStream.readAllBytes();
        inputStream.close();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
