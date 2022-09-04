package com.hepexta.rabbitmq.hometask.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {

    @Value("${service.rabbitmq.exchange}")
    private String exchange;

    private final AmqpTemplate template;

    public void publish(String routingQueue, Message<String> message) {
        template.convertAndSend(exchange, routingQueue, message);
        log.info("Message sent: {}", message);
    }
}
