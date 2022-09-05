package com.hepexta.rabbitmq.hometaskcustomretrywithdlx.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FailedOrderListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheStorage cacheStorage;

    @SneakyThrows
    @RabbitListener(queues = "${service.rabbitmq.failedOrderQueue}")
    public void listen(Message<String> in) {
        log.info("Order received: {}", in);
        Receipt receipt = objectMapper.readValue(in.getPayload(), Receipt.class);
        cacheStorage.storeFailed(receipt);
    }

}
