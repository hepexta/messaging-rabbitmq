package com.example.demo;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DemoVanillaApplication {

	public static void main(String[] args) throws Exception {
		var connectionFactory = new ConnectionFactory();
		connectionFactory.setVirtualHost("1-vanilla");
		//connectionFactory.setUsername(userName); "guest"
		//connectionFactory.setPassword(password); "guest"
		//connectionFactory.setVirtualHost(virtualHost); "/"
		//connectionFactory.setHost(hostName); "localhost"
		//connectionFactory.setPort(portNumber); 5672

		try (var connection = connectionFactory.newConnection()) {
			try (var channel = connection.createChannel()) {
				final var exchangeName = "demo-exchange";
				final var queue1 = "demo-queue1";
				final var queue2 = "demo-queue2";

				// declaration part start
				channel.exchangeDeclare(exchangeName, "topic", true);

				for (var queueName : List.of(queue1, queue2)) {
					final var routingKey = queueName; // for demo same as Q name
					channel.queueDeclare(queueName, true, false, false, null);
					channel.queueBind(queueName, exchangeName, routingKey);
				}
				// declaration part end

				// latch to hold the main thread until consumers received messages
				final var latch = new CountDownLatch(4);

				final var autoAck = false;

				// assign consumers to queues
				// because of consumers share same channel instance they should have different consumer tags
				channel.basicConsume(queue1, autoAck, "consumerTag1",
						new DemoConsumer(channel, "consumer1", latch));

				channel.basicConsume(queue2, autoAck, "consumerTag2",
						new DemoConsumer(channel, "consumer2", latch));

				// publish messages
				var routingKey = queue1;
				channel.basicPublish(exchangeName, routingKey, true,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						"Hello queue1".getBytes(StandardCharsets.UTF_8));

				routingKey = queue2;
				channel.basicPublish(exchangeName, routingKey, true,
						MessageProperties.PERSISTENT_TEXT_PLAIN,
						"Hello queue2".getBytes(StandardCharsets.UTF_8));

				// await consumers to process messages
				latch.await();
			}
		}
	}

	private static class DemoConsumer extends DefaultConsumer {

		private final String consumerName;

		private final CountDownLatch latch;

		public DemoConsumer(Channel channel, String consumerName, CountDownLatch latch) {
			super(channel);
			this.consumerName = consumerName;
			this.latch = latch;
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
				byte[] body) throws IOException {
			var routingKey = envelope.getRoutingKey();
			var contentType = properties.getContentType();
			var deliveryTag = envelope.getDeliveryTag();
			var redeliver = envelope.isRedeliver();
			var messageBody = new String(body, StandardCharsets.UTF_8);

			System.out.println(consumerName +
					", routingKey=" + routingKey +
					", contentType=" + contentType +
					", deliveryTag=" + deliveryTag +
					", redeliver=" + redeliver +
					", messageBody=" + messageBody);

			// positive ack
			//getChannel().basicAck(deliveryTag, false);

			if (!redeliver) {
				// nack with re-queue
				getChannel().basicNack(deliveryTag, false, true);
			} else {
				// nack and discard
				getChannel().basicNack(deliveryTag, false, false);
			}

			latch.countDown();
		}


	}



}
