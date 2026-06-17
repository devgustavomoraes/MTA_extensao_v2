package com.projetomta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjetoMtaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetoMtaApplication.class, args);
    }
}
