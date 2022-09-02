# demo-stream-function-ultimate

First example with custom retry logic is based on [Spring AMQP](https://docs.spring.io/spring-amqp/reference/html/) project.

## Messaging Topology
![image info](../images/custom retry.png)

`RunConfig.class` produces a receipt orders in a loop with 100 delay to **queue-order**

`OrderListener.class` listen the order and update the Status. After status is updated it publishes the message to **queue-receipt**

`ProcessedReceiptListenerWithRetry.class` listen the **queue-receipt** and retries if the Status is NOT Updated.

`CacheStorage.class` stores Updated receipts and failed (after 3 retry) to failedStore.