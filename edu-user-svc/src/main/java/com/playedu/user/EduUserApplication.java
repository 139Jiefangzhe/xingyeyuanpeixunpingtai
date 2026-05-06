package com.playedu.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.playedu")
@MapperScan("com.playedu.user.mapper")
public class EduUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduUserApplication.class, args);
    }
}
