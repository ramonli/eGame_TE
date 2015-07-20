package com.mpos.lottery.te.thirdpartyservice.amqp;

import java.io.IOException;

public interface MessagePublisher {
    /**
     * Publish message to a AMQP message broker.
     * 
     * @param message
     *            THe message in ProtoBuff encoding. The message will be durable.
     * @param exchangeName
     *            THe name of exchange.
     * @param routingKey
     *            THe routing key will used by exchange to determine which queue should receive this message.
     */
    void publish(byte[] message, String exchangeName, String routingKey) throws IOException;
}
