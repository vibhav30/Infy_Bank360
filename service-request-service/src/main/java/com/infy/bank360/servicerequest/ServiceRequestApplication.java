package com.infy.bank360.servicerequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.infy")
@EntityScan("com.infy.entity")
@EnableJpaRepositories("com.infy.repository")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.infy")
public class ServiceRequestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRequestApplication.class, args);
    }
}
