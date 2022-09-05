package com.hepexta.rabbitmq.hometaskcustomretrywithdlx.storage;

import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class CacheStorage {

    private final Set<com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt> storage = new CopyOnWriteArraySet<>();
    private final Set<com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt> failStorage = new CopyOnWriteArraySet<>();

    public void store(com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.Receipt receipt){
        storage.add(receipt);
        log.info("Storage Space: {}", storage.size());
    }

    public void storeFailed(Receipt receipt){
        failStorage.add(receipt);
        log.info("Fail Storage Space: {}", failStorage.size());
    }
}
