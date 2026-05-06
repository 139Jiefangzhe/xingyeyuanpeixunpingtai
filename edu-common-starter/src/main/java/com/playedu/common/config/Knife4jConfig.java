package com.playedu.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI commonOpenApi() {
        return new OpenAPI()
                .info(new Info().title("PlayEdu API").version("v1").description("PlayEdu common starter defaults"));
    }

    @Bean
    public GroupedOpenApi defaultGroupedOpenApi() {
        return GroupedOpenApi.builder().group("default").pathsToMatch("/**").build();
    }
}
