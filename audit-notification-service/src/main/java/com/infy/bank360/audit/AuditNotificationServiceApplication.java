package com.infy.bank360.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.infy"})
@EnableDiscoveryClient
public class AuditNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditNotificationServiceApplication.class, args);
    }
}
