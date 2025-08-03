package com.example.cp_main_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CpMainBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(CpMainBeApplication.class, args);
  }
}
