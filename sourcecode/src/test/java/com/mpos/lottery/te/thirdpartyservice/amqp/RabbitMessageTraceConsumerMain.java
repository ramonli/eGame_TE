package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.rabbitmq.client.QueueingConsumer.Delivery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * MUST enable the firehose of RabbitMQ by running 'rabbitmqctl trace_on' fist.
 * 
 * @author Ramon
 * 
 */
public class RabbitMessageTraceConsumerMain extends RabbitMessageConsumer {
    private static Log logger = LogFactory.getLog(RabbitMessageTraceConsumerMain.class);
    public static final String EXCHANGE_NAME = "amq.rabbitmq.trace";

    public RabbitMessageTraceConsumerMain(String host, boolean autoReconnect) {
        super(host, autoReconnect, "guest", "guest123");
    }

    @Override
    protected void consumeMessage(Delivery delivery) {
        String message = new String(delivery.getBody());
        String routingKey = delivery.getEnvelope().getRoutingKey();
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        logger.debug(" [x] Received [exchange:'" + headers.get("exchange_name") + "][routingkey:" + routingKey
                + "'] - '" + message + "'");
    }

    public static void main(String[] argv) throws Exception {
        RabbitMessageTraceConsumerMain main = new RabbitMessageTraceConsumerMain("192.168.2.188", false);

        main.consume(EXCHANGE_NAME, "te.message.trace", new String[] { "publish.*", "deliver.*" }, 1, false, true);
    }
}
