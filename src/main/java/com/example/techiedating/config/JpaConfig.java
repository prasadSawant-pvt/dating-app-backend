package com.example.techiedating.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.techiedating.repository",
    repositoryImplementationPostfix = "Impl"
)
public class JpaConfig {
    // Configuration class to enable JPA repositories and specify the implementation postfix
}
