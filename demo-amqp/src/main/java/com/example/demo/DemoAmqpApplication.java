package com.example.demo;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

@SpringBootApplication
public class DemoAmqpApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoAmqpApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(AmqpTemplate template) {
		return args -> {
			template.convertAndSend("demo-exchange", "routing-queue1", "Hello queue1");
			template.convertAndSend("demo-exchange", "routing-queue2", "Hello queue2");
		};
	}

	@RabbitListener(
			bindings = @QueueBinding(
					value = @Queue(name = "demo-queue1", durable = "true"),
					key = "routing-queue1",
					exchange = @Exchange(name = "demo-exchange", type = ExchangeTypes.TOPIC))
	)
	public void listenQueue1(Message<String> in) {
		doListen("consumer1", in);
	}

	@RabbitListener(
			bindings = @QueueBinding(
					value = @Queue(name = "demo-queue2", durable = "true"),
					key = "routing-queue2",
					exchange = @Exchange(name = "demo-exchange", type = ExchangeTypes.TOPIC))
	)
	public void listenQueue2(Message<String> in) {
		doListen("consumer2", in);
	}

	private static void doListen(String consumerName, Message<String> in) {
		System.out.println(consumerName +
				", headers=" + in.getHeaders() +
				", payload=" + in.getPayload());

		var red = in.getHeaders().get("amqp_redelivered", Boolean.class);
		if (red != null && Boolean.TRUE.equals(red)) {
			throw new AmqpRejectAndDontRequeueException("nack and discard");
		}
		throw new ImmediateRequeueAmqpException("nack with re-queue");
	}

}
