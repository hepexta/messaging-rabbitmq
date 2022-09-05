package com.hepexta.rabbitmq.hometaskdlx.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometaskdlx.model.Receipt;
import com.hepexta.rabbitmq.hometaskdlx.model.UpdateStatus;
import com.hepexta.rabbitmq.hometaskdlx.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final CacheStorage cacheStorage;

    @SneakyThrows
    @RabbitListener(queues = "${service.rabbitmq.orderRoutingQueue}")
    public void listen(Message<String> in) {
        log.info("Order received: {}", in);
        processMessage(in);
    }

    private void processMessage(Message<String> in) throws JsonProcessingException {
        Receipt receipt = objectMapper.readValue(in.getPayload(), Receipt.class);
        receipt.setStatus(getRandomStatus());
        if (!receipt.getStatus().equals(UpdateStatus.UPDATED)) {
            throw new AmqpRejectAndDontRequeueException("nack and discard");
        }
        cacheStorage.store(receipt);
    }

    private UpdateStatus getRandomStatus() {
        UpdateStatus value = UpdateStatus.values()[random.nextInt(3)];
        log.info("Status changes to: {}", value);
        return value;
    }

}
