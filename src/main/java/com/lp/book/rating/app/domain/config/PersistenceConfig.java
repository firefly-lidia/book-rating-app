package com.lp.book.rating.app.domain.config;

import com.lp.book.rating.app.domain.repository.BookRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

@Configuration
@EntityScan(value = "com.lp.book.rating.app.domain")
@EnableJpaRepositories(basePackageClasses = BookRepository.class)
@EnableJpaAuditing(modifyOnCreate = false, auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

    private static final String DEFAULT_AUDITING_USERNAME = "RATING-SYSTEM";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .or(() -> Optional.of(DEFAULT_AUDITING_USERNAME));
    }

}
