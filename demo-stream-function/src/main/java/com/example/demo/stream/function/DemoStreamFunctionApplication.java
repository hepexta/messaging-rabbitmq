package com.example.demo.stream.function;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@SpringBootApplication
public class DemoStreamFunctionApplication {

	private static final String ROUTING_KEY_HEADER = "myRoutingKey";

	public static void main(String[] args) {
		SpringApplication.run(DemoStreamFunctionApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(StreamBridge streamBridge) {
		return args -> {
			streamBridge.send("source-out-0",
					MessageBuilder
							.withPayload("Hello queue1")
							.setHeader(ROUTING_KEY_HEADER, "routing-queue1")
							.build());
			streamBridge.send("source-out-0",
					MessageBuilder
							.withPayload("Hello queue2")
							.setHeader(ROUTING_KEY_HEADER, "routing-queue2")
							.build());
		};
	}

	/*@StreamListener("testSource-in-0")
	public void processMessage(String message) {
		queue1Sink().accept(message);
	}*/

	@Bean
	public Consumer<String> queue1Sink() {
		return payload -> System.out.println("Consumer1: " + payload);
	}

	@Bean
	public Consumer<String> queue2Sink() {
		return payload -> System.out.println("Consumer2: " + payload);
	}

}
