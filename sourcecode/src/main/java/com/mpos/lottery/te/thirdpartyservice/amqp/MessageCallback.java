package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

public interface MessageCallback {

    /**
     * Do real work, such as publish or consume.
     * 
     * @throws ShutdownSignalException
     *             when channel or underlying connection is closed.
     * @throws IOException
     *             when connection is closed.
     * @throws InterruptedException
     *             when consumer thread is interrupted(<code>consumer.nextDelivery()</code>)
     */
    void doExchange(MessageContext context) throws ShutdownSignalException, IOException, InterruptedException;
}
