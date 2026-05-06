package com.playedu.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.playedu")
@MapperScan("com.playedu.exam.mapper")
@EnableFeignClients(basePackages = "com.playedu.exam.feign")
public class EduExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduExamApplication.class, args);
    }
}
