package com.hepexta.rabbitmq.hometask.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometask.model.Receipt;
import com.hepexta.rabbitmq.hometask.model.UpdateStatus;
import com.hepexta.rabbitmq.hometask.publisher.MessagePublisher;
import com.hepexta.rabbitmq.hometask.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReceiptListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String RETRY_COUNT_FIELD = "retryCount";
    @Value("${service.rabbitmq.retryCount:3}")
    private Integer retryCount;
    private final CacheStorage cacheStorage;
    private final MessagePublisher messagePublisher;

    @SneakyThrows
    @RabbitListener(queues = "${service.rabbitmq.routingQueue}")
    public void listen(Message<String> in) {
        log.info("Message received: {}", in);
        Receipt receipt = objectMapper.readValue(in.getPayload(), Receipt.class);
        if (receipt.getStatus().equals(UpdateStatus.UPDATED)) {
            cacheStorage.store(receipt);
        }
        else {
            Integer inRetryCount = Optional.ofNullable(in.getHeaders().get(RETRY_COUNT_FIELD, Integer.class)).orElse(0);
            if (inRetryCount < retryCount) {
                messagePublisher.publish(new GenericMessage<>(in.getPayload(), Map.of(RETRY_COUNT_FIELD, ++inRetryCount)));
            }
            else {
                cacheStorage.storeFailed(receipt);
            }
        }
    }
}
