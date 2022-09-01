package com.example.demo.stream.function.retry;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SpringBootApplication
public class DemoStreamFunctionWithRetryApplication {

	private static final String ROUTING_KEY_HEADER = "myRoutingKey";

	public static void main(String[] args) {
		SpringApplication.run(DemoStreamFunctionWithRetryApplication.class, args);
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

	// wellll, I'm probably need to open a jira ticket in spring-cloud-steam-rabbitmq repository
	@Bean
	@ConditionalOnProperty(name = "republish", havingValue = "true")
	public DeclarableCustomizer declarableCustomizer() {
		return declarable -> {
			if (declarable instanceof Queue) {
				var queue = (Queue) declarable;
				if (queue.getName().equals("demo-queue1")
						|| queue.getName().equals("demo-queue2")) {
					queue.removeArgument("x-dead-letter-exchange");
					queue.removeArgument("x-dead-letter-routing-key");
				}
			}
			return declarable;
		};
	}

	@Bean
	public Consumer<Message<String>> queue1Sink() {
		return new DemoConsumer("Consumer1");
	}

	@Bean
	public Consumer<Message<String>> queue2Sink() {
		return new DemoConsumer("Consumer2");
	}

	private static class DemoConsumer implements Consumer<Message<String>> {

		private final String consumerName;

		private DemoConsumer(String consumerName) {
			this.consumerName = consumerName;
		}

		@Override
		public void accept(Message<String> in) {
			System.out.println(consumerName +
					", payload=" + in.getPayload() +
					", headers=" + in.getHeaders());
			var deathHeader = in.getHeaders().get("x-death", List.class);
			var death = deathHeader != null && deathHeader.size() > 0
					? (Map<String, Object>)deathHeader.get(0)
					: null;
			if (death != null && (long) death.get("count") > 2) {
				// giving up - don't send to DLX
				throw new ImmediateAcknowledgeAmqpException("Failed after 3 attempts");
			}
			// nack and do not re-queue
			throw new AmqpRejectAndDontRequeueException("failed");
		}
	}

}
