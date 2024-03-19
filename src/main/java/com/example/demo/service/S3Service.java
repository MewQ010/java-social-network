package com.example.demo.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.demo.config.AmazonS3Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

        @Autowired
        AmazonS3Config amazonS3Config;

        private String bucketName = "intitajavaproject";
        Regions regions = Regions.EU_NORTH_1;

        public void uploadToS3(InputStream inputStream, String fileName) throws IOException, AmazonServiceException, SdkClientException {
            AmazonS3 s3Client = amazonS3Config.amazonS3Client();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image.jpg");
            metadata.setContentLength(inputStream.available());

            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
            s3Client.putObject(request);
        }

}
