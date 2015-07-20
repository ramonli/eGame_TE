package com.mpos.lottery.te.thirdpartyservice.amqp;

import java.io.IOException;

public interface MessageConsumer {

    /**
     * A convenient method to simply call {@link #consume(String, String, String[], int, boolean, boolean)} with
     * {@code declareExchange=true} and {@code declareQueue=true}
     */
    void consume(String exchangeName, String queueName, String[] routingKeys, int qos) throws IOException,
            InterruptedException;

    /**
     * Consume message from AMQP broker.
     * 
     * @param exchangeName
     *            THe exchange to which the queue will be bound.
     * @param queueName
     *            THe name of queue from which the client consumes message.
     * @param routingKeys
     *            THe routing keys for binding between queue and exchange.
     * @param qos
     *            How many messages should be pre-fetched.
     * @param declareExchange
     *            Whether declare this exchange with provided {@code exchangeName}. In some case, the exchange has been
     *            declared by other system.
     * @param declareQueue
     *            Whether declare the queue by provided {@code queueName}. In some cases, the queue has been declared by
     *            other systems.
     * @throws IOException
     *             will be thrown out when consume remote message.
     */
    public void consume(final String exchangeName, final String queueName, final String[] routingKeys, final int qos,
            boolean declareExchange, boolean declareQueue) throws IOException, InterruptedException;
}
