package com.example.demo.stream.ultimate;

import org.apache.commons.lang3.ThreadUtils;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@SpringBootApplication
public class DemoStreamFunctionUltimateApplication {

    private static final String ROUTING_KEY_HEADER = "myRoutingKey";

    public static void main(String[] args) {
        SpringApplication.run(DemoStreamFunctionUltimateApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(StreamBridge streamBridge) {
        return args -> {
            IntStream.range(0, 20).forEach(i -> {
                streamBridge.send("source-out-0",
                        MessageBuilder
                                .withPayload("Hello queue1")
                                .setHeader(ROUTING_KEY_HEADER, "routing-queue1")
                                .build());
            });
            /*streamBridge.send("source-out-0",
                    MessageBuilder
                            .withPayload("Hello queue2")
                            .setHeader(ROUTING_KEY_HEADER, "routing-queue2")
                            .build());*/
        };

    }

    @Bean
    // wellll, I'm probably need to open a jira ticket in spring-cloud-steam-rabbitmq repository
    public DeclarableCustomizer declarableCustomizer() {
        return declarable -> {
            if (declarable instanceof Queue) {
                var queue = (Queue) declarable;
                if (queue.getName().equals("demo-queue1")
                        || queue.getName().equals("demo-queue2")) {
                    queue.removeArgument("x-dead-letter-exchange");
                    queue.removeArgument("x-dead-letter-routing-key");

                    queue.addArgument("x-dead-letter-exchange", "deadletter-exchange");
                }
            }
            return declarable;
        };
    }

    @Bean
    public Consumer<Message<String>> queue1Sink(StreamBridge streamBridge) {
        return in -> safeSleep(Duration.ofSeconds(5));
        //return new DemoConsumer("Consumer1", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> queue2Sink(StreamBridge streamBridge) {
        return new DemoConsumer("Consumer2", streamBridge);
    }

    @Bean
    public Consumer<Message<String>> failedSink() {
        return in -> System.out.println("FAILED!!!! " + in.toString());
    }

    @Bean
    public Consumer<Message<String>> deadletterSink() {
        return in -> System.out.println("DEADLETTER!!!! " + in.toString());
    }

    private static class DemoConsumer implements Consumer<Message<String>> {

        private final String consumerName;

        private final StreamBridge streamBridge;

        private DemoConsumer(String consumerName, StreamBridge streamBridge) {
            this.consumerName = consumerName;
            this.streamBridge = streamBridge;
        }

        @Override
        public void accept(Message<String> in) {
            System.out.println(consumerName +
                    ", payload=" + in.getPayload() +
                    ", headers=" + in.getHeaders());
            var deathHeader = in.getHeaders().get("x-death", List.class);
            var death = deathHeader != null && deathHeader.size() > 0
                    ? (Map<String, Object>) deathHeader.get(0)
                    : null;
            if (death != null && (long) death.get("count") > 2) {
                // giving up - don't send to DLX, instead send to failed exchange
                // TODO think about copying headers with exception details
                streamBridge.send("failed-out-0",
                        MessageBuilder
                                .withPayload(in.getPayload())
                                .build());
                throw new ImmediateAcknowledgeAmqpException("Failed after 3 attempts");
            }
            // nack and do not re-queue
            throw new AmqpRejectAndDontRequeueException("failed");
        }
    }

    private static void safeSleep(Duration duration) {
        try {
            ThreadUtils.sleep(duration);
        } catch (InterruptedException ignore) {
        }
    }

}
