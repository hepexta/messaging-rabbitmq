package com.hepexta.rabbitmq.hometask.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometask.model.Receipt;
import com.hepexta.rabbitmq.hometask.model.UpdateStatus;
import com.hepexta.rabbitmq.hometask.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final MessagePublisher messagePublisher;

    @Value("${service.rabbitmq.processedRoutingQueue}")
    private String processedRoutingQueue;

    @SneakyThrows
    @RabbitListener(queues = "${service.rabbitmq.orderRoutingQueue}")
    public void listen(Message<String> in) {
        log.info("Order received: {}", in);
        processMessage(in);
    }

    private void processMessage(Message<String> in) throws JsonProcessingException {
        Receipt receipt = objectMapper.readValue(in.getPayload(), Receipt.class);
        receipt.setStatus(getRandomStatus());
        String payload = objectMapper.writeValueAsString(receipt);
        messagePublisher.publish(processedRoutingQueue, new GenericMessage(payload, in.getHeaders()));
    }

    private UpdateStatus getRandomStatus() {
        int i = getRandomNumber(1, 4);
        UpdateStatus value = UpdateStatus.values()[i];
        log.info("Status changes to: {}, {}", value, i);
        return value;
    }

    public int getRandomNumber(int min, int max) {
        return random.nextInt(3);
    }
}
