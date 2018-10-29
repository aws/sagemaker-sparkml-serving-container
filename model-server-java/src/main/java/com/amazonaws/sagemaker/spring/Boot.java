package com.amazonaws.sagemaker.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Boot {
    public static void main(String[] args) {
        SpringApplication.run(Boot.class, args);
    }
}