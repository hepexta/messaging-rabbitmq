package com.hepexta.rabbitmq.hometaskcustomretrywithdlx.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.publisher.MessagePublisher;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.UpdateStatus;
import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String RETRY_COUNT_FIELD = "retryCount";
    private final Random random = new Random();
    private final MessagePublisher messagePublisher;
    private final CacheStorage cacheStorage;
    @Value("${service.rabbitmq.retryCount:3}")
    private Integer retryCount;
    @Value("${service.rabbitmq.orderRoutingQueue}")
    private String orderRoutingQueue;

    @SneakyThrows
    @RabbitListener(queues = "${service.rabbitmq.orderRoutingQueue}")
    public void listen(Message<String> in) {
        log.info("Order received: {}", in);
        processMessage(in);
    }

    private void processMessage(Message<String> in) throws JsonProcessingException {
        Receipt receipt = objectMapper.readValue(in.getPayload(), Receipt.class);
        receipt.setStatus(getRandomStatus());
        if (com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.UpdateStatus.UPDATED.equals(receipt.getStatus())) {
            cacheStorage.store(receipt);
        }
        else {
            Integer inRetryCount = Optional.ofNullable(in.getHeaders().get(RETRY_COUNT_FIELD, Integer.class)).orElse(0);
            if (inRetryCount < retryCount) {
                messagePublisher.publish(orderRoutingQueue, new GenericMessage<>(in.getPayload(), Map.of(RETRY_COUNT_FIELD, ++inRetryCount)));
            }
            else {
                throw new AmqpRejectAndDontRequeueException("nack and discard");
            }
        }
    }

    private com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.UpdateStatus getRandomStatus() {
        com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.UpdateStatus value = UpdateStatus.values()[random.nextInt(3)];
        log.info("Status changes to: {}", value);
        return value;
    }

}
