package com.mars.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mars.system", "com.mars.common"})
public class RateLimitTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitTestApplication.class, args);
    }
}
