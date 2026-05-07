package com.playedu.point;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.playedu.point.mapper")
public class EduPointApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduPointApplication.class, args);
    }
}

