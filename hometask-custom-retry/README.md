# Custom Retry

First example with custom retry logic is based on [Spring AMQP](https://docs.spring.io/spring-amqp/reference/html/) project.

Failed Message Exchange is not configured as DLX for the source queues.
Consumer is responsible to re-publish failed messages.

## Messaging Topology
![image info](../images/custom_retry.png)

`RunConfig.class` produces a receipt orders in a loop with 100 delay to **queue-order**

`OrderListener.class` listen the order and update the Status. If status is not UPDATED its republishes to the same queue.

`CacheStorage.class` stores Updated receipts and failed (after 3 retry) to failedStore.
