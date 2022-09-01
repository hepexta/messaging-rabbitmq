package com.hepexta.rabbitmq.hometask.storage;

import com.hepexta.rabbitmq.hometask.model.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class CacheStorage {

    private final Set<Receipt> storage = new CopyOnWriteArraySet<>();
    private final Set<Receipt> failStorage = new CopyOnWriteArraySet<>();

    public void store(Receipt receipt){
        storage.add(receipt);
        log.info("Storage Space: {}", storage.size());
    }

    public void storeFailed(Receipt receipt){
        failStorage.add(receipt);
        log.info("Fail Storage Space: {}", failStorage.size());
    }
}
