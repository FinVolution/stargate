package com.ppdai.stargate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.ppdai.stargate.dao")
@EnableJpaAuditing
public class JpaConfiguration {

    @Bean
    AuditorAware<String> auditorProvider() {
        return new UserAuditorAware();
    }

}
