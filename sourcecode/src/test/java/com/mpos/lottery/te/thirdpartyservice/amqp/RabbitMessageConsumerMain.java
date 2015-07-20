package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.mpos.lottery.te.trans.domain.TransactionType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RabbitMessageConsumerMain extends RabbitMessageConsumer {
    private static Log logger = LogFactory.getLog(RabbitMessageConsumerMain.class);
    public static final String EXCHANGE_NAME = "TE." + TransactionType.TRANSFER_CREDIT;

    public RabbitMessageConsumerMain(String host, boolean autoReconnect) {
        super(host, autoReconnect, "guest", "guest123");
    }

    @Override
    protected void consumeMessage(Delivery delivery) {
        String message = new String(delivery.getBody());
        String routingKey = delivery.getEnvelope().getRoutingKey();
        logger.debug(" [x] Received '" + routingKey + "':'" + message + "'");
    }

    public static void main(String[] argv) throws Exception {
        // RabbitMessageConsumerMain main = new
        // RabbitMessageConsumerMain("192.168.2.188", true);
        RabbitMessageConsumerMain main = new RabbitMessageConsumerMain("localhost", true);
        main.consume(EXCHANGE_NAME, RabbitMessagePublisher.class.getName(), new String[] { EXCHANGE_NAME + ".*" }, 1);
        // deleteQueue();
    }

    public static void deleteQueue() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel = connection.createChannel();
        channel.queueDelete(RabbitMessagePublisher.class.getName());
        connection.close();
    }
}
