package com.cafeadmin.floor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cafeadmin")
public class FloorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FloorApplication.class, args);
    }
}
