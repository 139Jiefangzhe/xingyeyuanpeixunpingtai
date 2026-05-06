package com.playedu.course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.playedu")
@MapperScan("com.playedu.course.mapper")
public class EduCourseApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduCourseApplication.class, args);
    }
}
