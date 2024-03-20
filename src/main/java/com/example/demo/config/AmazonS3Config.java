package com.example.demo.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {
    @Bean
    public AmazonS3 amazonS3Client() {
        Regions regions = Regions.EU_NORTH_1;

        AWSCredentials credentials = new BasicAWSCredentials("AKIAU6GDWNCHO7MYIBYO", "ZZkV6Es7m/QXnYq8FOm2h9M3z89uIra/mP06yRgT");

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(regions)
                .build();
    }
}
