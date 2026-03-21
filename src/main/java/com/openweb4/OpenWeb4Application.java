package com.openweb4;

import com.openweb4.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class OpenWeb4Application {

    public static void main(String[] args) {
        SpringApplication.run(OpenWeb4Application.class, args);
    }
}
