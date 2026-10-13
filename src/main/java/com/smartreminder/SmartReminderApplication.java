package com.smartreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Reminder Application - Main Entry Point
 * 
 * @SpringBootApplication enables:
 * 1. @Configuration - Tags the class as a source of bean definitions
 * 2. @EnableAutoConfiguration - Tells Spring Boot to start adding beans based on classpath settings
 * 3. @ComponentScan - Tells Spring to look for other components, configurations, and services in this package
 */
@SpringBootApplication
public class SmartReminderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartReminderApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" Smart Reminder Backend Started Successfully!   ");
        System.out.println(" Server URL: http://localhost:8080               ");
        System.out.println(" Health Check: http://localhost:8080/api/health  ");
        System.out.println("=================================================");
    }
}
