package com.example.techiedating.config;

import com.example.techiedating.service.ILocationIQService;
import com.example.techiedating.service.DefaultLocationIQService;
import com.example.techiedating.service.NoOpLocationIQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LocationIQServiceConfig {

    @Bean
    @ConditionalOnBean(LocationIQConfig.class)
    public ILocationIQService locationIQService(LocationIQConfig locationIQConfig, RestTemplate restTemplate) {
        return new DefaultLocationIQService(restTemplate, locationIQConfig);
    }
    
    @Bean
    @ConditionalOnMissingBean(ILocationIQService.class)
    public ILocationIQService noOpLocationIQService() {
        return new NoOpLocationIQService();
    }
}
