package com.hepexta.rabbitmq.hometaskdlx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometaskdlx.model.Receipt;
import com.hepexta.rabbitmq.hometaskdlx.publisher.MessagePublisher;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${service.rabbitmq.orderRoutingQueue}")
    private String orderRoutingQueue;
    
    @SneakyThrows
    @Bean
    public ApplicationRunner runner() {
        return args -> {
            while (true) {
                String order = objectMapper.writeValueAsString(prepareReceipt());
                messagePublisher.publish(orderRoutingQueue, new GenericMessage(order, Map.of("retryCount", 0)));
                Thread.sleep(1000);
            }
        };
    }

    private Receipt prepareReceipt() {
        return Receipt.builder()
                .good("default")
                .customer(UUID.randomUUID().toString())
                .price(BigDecimal.valueOf(Math.random()))
                .build();
    }
}
