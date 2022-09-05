package com.hepexta.rabbitmq.hometask;

import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.HometaskCustomRetryWithDlxApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HometaskCustomRetryApplication {
    public static void main(String[] args) {
        SpringApplication.run(HometaskCustomRetryWithDlxApplication.class, args);
    }
}
