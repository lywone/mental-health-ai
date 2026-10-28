package com.mental.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 心理健康AI助手 后端服务入口
 */
@EnableScheduling
@SpringBootApplication
public class MentalHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentalHealthApplication.class, args);
    }
}
