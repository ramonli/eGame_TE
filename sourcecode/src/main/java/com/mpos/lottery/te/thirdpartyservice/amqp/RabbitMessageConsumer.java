package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public abstract class RabbitMessageConsumer implements MessageConsumer {
    private Log logger = LogFactory.getLog(RabbitMessageConsumer.class);
    private String host;
    // whether re-connect AMQP broker if connection is broken.
    private boolean autoReconnect = false;
    private long reconnectInterval = 60 * 1000;
    private String username;
    private String password;

    public RabbitMessageConsumer(String host, boolean autoReconnect, String username, String password) {
        super();
        this.host = host;
        this.autoReconnect = autoReconnect;
        this.username = username;
        this.password = password;
    }

    @Override
    public void consume(final String exchangeName, final String queueName, final String[] routingKeys, final int qos)
            throws IOException, InterruptedException {
        this.consume(exchangeName, queueName, routingKeys, qos, true, true);
    }

    @Override
    public void consume(final String exchangeName, final String queueName, final String[] routingKeys, final int qos,
            boolean declareExchange, boolean declareQueue) throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.getHost());
        factory.setUsername(this.getUsername());
        factory.setPassword(this.getPassword());

        while (true) {
            Connection connection = null;
            Channel channel = null;
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();

                if (declareExchange) {
                    channel.exchangeDeclare(exchangeName, "topic", true);
                }
                if (declareQueue) {
                    // declare a durable, non-exclusive, non-autodelete queue.
                    channel.queueDeclare(queueName, true, false, false, null);
                }
                for (String routingKey : routingKeys) {
                    channel.queueBind(queueName, exchangeName, routingKey);
                }
                // distribute workload among all consumers, consumer will
                // pre-fetch
                // {qos}
                // messages to local buffer.
                channel.basicQos(qos);

                logger.debug(" [*] Waiting for messages. To exit press CTRL+C");

                QueueingConsumer consumer = new QueueingConsumer(channel);
                // disable auto-ack. If enable auto-ack, RabbitMQ delivers a
                // message to
                // the customer it immediately removes it from memory.
                boolean autoAck = false;
                channel.basicConsume(queueName, autoAck, consumer);

                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    try {
                        RabbitMessageConsumer.this.consumeMessage(delivery);
                    } catch (Exception e) {
                        // the exception shouldn't affect the next message
                        logger.info("[IGNORE]" + e.getMessage());
                    }
                    /**
                     * If no call to basicAck here, once consumer close connection, broker will move the unacked message
                     * to redeliver queue.
                     */
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            } catch (Exception e) {
                logger.warn(e);
            }

            if (autoReconnect) {
                this.release(connection, channel);
                logger.info("[*] Will try to reconnect to remote host(" + this.getHost() + ") in "
                        + this.reconnectInterval / 1000 + " seconds.");
                Thread.sleep(this.getReconnectInterval());
            } else {
                break;
            }
        }
    }

    /**
     * Subclass should implement this method to consume message.
     */
    protected abstract void consumeMessage(QueueingConsumer.Delivery delivery);

    private void release(Connection conn, Channel channel) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (channel != null) {
                channel.abort();
            }
            conn = null;
            channel = null;
        } catch (Exception e) {
            // simply ignore this exception
            logger.warn(e.getCause() != null ? e.getCause() : e);
        }
    }

    public String getHost() {
        return host;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(long reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
