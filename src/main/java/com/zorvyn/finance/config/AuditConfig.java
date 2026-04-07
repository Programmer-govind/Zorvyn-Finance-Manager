package com.zorvyn.finance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // Enables @CreatedDate and @LastModifiedDate on BaseEntity.
    
}
