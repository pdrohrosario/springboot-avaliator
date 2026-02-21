package com.project.feedbackservice.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.ErrorDecoder;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public ErrorDecoder customFeignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}