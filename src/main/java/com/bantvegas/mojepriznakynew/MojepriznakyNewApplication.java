package com.bantvegas.mojepriznakynew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
public class MojepriznakyNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(MojepriznakyNewApplication.class, args);
    }

}
