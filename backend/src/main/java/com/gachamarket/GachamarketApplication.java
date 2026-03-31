package com.gachamarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GachamarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GachamarketApplication.class, args);
    }
}
