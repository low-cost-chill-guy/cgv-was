//package com.cloudwave.lowcostchillguy.user.config;
//
//import com.amazonaws.services.lambda.AWSLambda;
//import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
//import lombok.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class AWSConfig {
//    @Value("${aws.region}")
//    private String region;
//
//    @Bean
//    public AWSLambda awsLambda() {
//        return AWSLambdaClientBuilder.standard()
//                .withRegion(region)
//                .build();
//    }
//}