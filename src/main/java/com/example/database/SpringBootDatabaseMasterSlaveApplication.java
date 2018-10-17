package com.example.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, org.activiti.spring.boot.SecurityAutoConfiguration.class})
// @EnableAsync
// @ImportResource(locations = {"classpath:spring-jpa.xml"})
public class SpringBootDatabaseMasterSlaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDatabaseMasterSlaveApplication.class, args);
    }
}
