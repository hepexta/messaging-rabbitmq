package com.hepexta.rabbitmq.hometask.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometask.model.Receipt;
import com.hepexta.rabbitmq.hometask.model.UpdateStatus;
import com.hepexta.rabbitmq.hometask.publisher.MessagePublisher;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.GenericMessage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Configuration
public class RunConfig {
    
    @Autowired
    private MessagePublisher messagePublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @SneakyThrows
    @Bean
    public ApplicationRunner runner() {
        return args -> {
            while (true) {
                for (UpdateStatus value : UpdateStatus.values()) {
                    String payload = objectMapper.writeValueAsString(prepareReceipt(value));
                    messagePublisher.publish(new GenericMessage(payload, Map.of("retryCount", 0)));
                    Thread.sleep(1000);
                }
            }
        };
    }

    private Receipt prepareReceipt(UpdateStatus value) {
        return Receipt.builder()
                .good("default")
                .customer(UUID.randomUUID().toString())
                .price(BigDecimal.valueOf(Math.random()))
                .status(value).build();
    }
}
