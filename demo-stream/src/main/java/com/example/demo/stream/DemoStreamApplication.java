package com.example.demo.stream;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@EnableBinding({Source.class, DemoStreamApplication.CustomSink.class })
public class DemoStreamApplication {

	private static final String ROUTING_KEY_HEADER = "myRoutingKey";

	public interface CustomSink {

		String QUEUE1 = "input-queue1";

		@Input(QUEUE1)
		SubscribableChannel inputQueue1();

		String QUEUE2 = "input-queue2";

		@Input(QUEUE2)
		SubscribableChannel inputQueue2();

	}

	public static void main(String[] args) {
		SpringApplication.run(DemoStreamApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(Source source) {
		return args -> {
			source.output().send(
					MessageBuilder
							.withPayload("Hello queue1")
							.setHeader(ROUTING_KEY_HEADER, "routing-queue1")
							.build());
			source.output().send(
					MessageBuilder
							.withPayload("Hello queue2")
							.setHeader(ROUTING_KEY_HEADER, "routing-queue2")
							.build());
		};

	}

	@StreamListener(CustomSink.QUEUE1)
	public void listenQueue1(Message<String> in) {
		System.out.println("Consumer1: " + in.getPayload());
		throw new RuntimeException("Demo exception");
	}

	@StreamListener(CustomSink.QUEUE2)
	public void listenQueue2(Message<String> in) {
		System.out.println("Consumer2: " + in.getPayload());
	}

}
