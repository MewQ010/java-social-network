package com.example.demo.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.FileCompressor.CustomMultipartFileFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AWSService {

//    @Value("${application.bucket.name}")
//    private String bucketName;

    private final S3Service s3Service;
    private final AmazonS3 amazonS3;
    private final CustomMultipartFileFactory customMultipartFileFactory;

    public boolean doesObjectExist(String bucketName, String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    //TODO  rename function! This is quality downside - not compression
    private byte[] compressImage(MultipartFile mpFile) throws LoginException {
        float quality = 0.05f;
        String imageName = mpFile.getOriginalFilename();
        String imageExtension = imageName.substring(imageName.lastIndexOf(".") + 1);
        ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(imageExtension).next();
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(quality);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(baos);
        imageWriter.setOutput(imageOutputStream);
        BufferedImage originalImage = null;
        try (InputStream inputStream = mpFile.getInputStream()) {
            originalImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            String info = String.format("compressImage - bufferedImage (file %s)- IOException - message: %s ", imageName, e.getMessage());
            throw new LoginException(info);
        }
        IIOImage image = new IIOImage(originalImage, null, null);
        try {
            imageWriter.write(null, image, imageWriteParam);
        } catch (IOException e) {
            String info = String.format("compressImage - imageWriter (file %s)- IOException - message: %s ", imageName, e.getMessage());
            throw new LoginException(info);
        } finally {
            imageWriter.dispose();
        }
        return baos.toByteArray();
    }

    public String saveImageToAWS(MultipartFile multipartFile, String login) throws LoginException, IOException {
        String profileImage = multipartFile.getOriginalFilename().replaceAll("\\.jpg", "") + login + "\\.jpg";
        byte[] compressedFile = compressImage(multipartFile);
        multipartFile = customMultipartFileFactory.create(compressedFile, multipartFile.getName(), multipartFile.getOriginalFilename(), multipartFile.getContentType());
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
