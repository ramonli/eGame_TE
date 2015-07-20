package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class RabbitMessagePublishMain {
    private static Log logger = LogFactory.getLog(RabbitMessagePublishMain.class);
    public static final String EXCHANGE_NAME = RabbitMessageConsumerMain.EXCHANGE_NAME;
    private String host;
    private String exchangeName;

    public RabbitMessagePublishMain(String host, String exchangeName) {
        this.host = host;
        this.exchangeName = exchangeName;
    }

    protected void publish(byte[] message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.getHost());
        factory.setVirtualHost("/");
        factory.setUsername("guest");
        factory.setPassword("guest123");
        final Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // declare a 'topic' type of exchange
        channel.exchangeDeclare(this.exchangeName, "topic", true);
        // If a message is published with the "mandatory" flags set, but cannot
        // be routed, the broker will return it to the sending client (via a
        // AMQP.Basic.Return command).
        channel.addReturnListener(new ReturnListener() {

            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                    AMQP.BasicProperties properties, byte[] body) throws IOException {
                logger.warn("[X]Returned message(replyCode:" + replyCode + ",replyText:" + replyText + ",exchange:"
                        + exchange + ",routingKey:" + routingKey + ",body:" + new String(body));

                /**
                 * You can't close connection before receive the RETURN or DELIVER command which issued by RabbitMQ
                 * broker, otherwise no any listener will be invoked if events reach after the closure of connection.
                 * <b>And no any exception thrown out in this case...what a pitty!</b>
                 * <p/>
                 * OH, also can't close connection here, as if we put a channel into confirm mode, broker will mark a
                 * message as Acked if decides a message will not be routed to any queues. In this case both
                 * <code>ReturnListener</code> and
                 * <code>ConfirmListener<code> will be called, if close connection here, <code>ConfirmListener</code>
                 * may can't work.
                 * <p/>
                 * Can close connection after call Channel#waitForConfirmsOrDie()
                 */
                // RabbitMessagePublishMain.this.release(connection);
            }

        });

        // NOTE: must call this method to enables publisher acknowledgements on
        // this
        // channel, otherwise the publisher process will be blocked for ever if
        // message is delivered successfully.
        channel.confirmSelect();
        /**
         * Implement this interface in order to be notified of Confirm events. Acks represent messages handled
         * succesfully; Nacks represent messages lost by the broker. Note, the lost messages could still have been
         * delivered to consumers, but the broker cannot guarantee this.
         * <p/>
         * Refer to http://www.rabbitmq.com/confirms.html
         */
        channel.addConfirmListener(new ConfirmListener() {

            /**
             * <ol>
             * <li>it decides a message will not be routed to queues (if the mandatory flag is set then the basic.return
             * is sent first) or
             * <li>a message has reached all of the queues (and their mirrors) it was routed to, and, if it requires
             * persistence (i.e. the message has been marked as persistent and the queue is durable), either a) has been
             * persisted to disk, or b) has been consumed and, if necessary, acknowledged.</li>
             * </ol>
             */
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                logger.info("Ack: " + deliveryTag);
                // RabbitMessagePublishMain.this.release(connection);
            }

            /**
             * However in which this method will be called??
             */
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                logger.info("Nack: " + deliveryTag);
                // RabbitMessagePublishMain.this.release(connection);
            }

        });

        /**
         * If set 'immediate' to true, the publisher will throw
         * " com.rabbitmq.client.AlreadyClosedException: clean connection shutdown; reason: Attempt to use closed channel"
         * , as the server will immediately close the connection once received message.
         * <p/>
         * Diff it with the case where no 'immediate' set, in fact RabbitMQ doesn't support this parameter.
         */
        // Content-type "application/octet-stream", deliveryMode 2
        // (persistent), priority zero
        // channel.basicPublish(this.exchangeName, "XX." +
        // RabbitMessageConsumerMain.EXCHANGE_NAME + ".-1",
        // true, MessageProperties.PERSISTENT_BASIC, message);
        channel.basicPublish(this.exchangeName, RabbitMessageConsumerMain.EXCHANGE_NAME + ".-1", true,
                MessageProperties.PERSISTENT_BASIC, message);
        // connection.close();

        /**
         * Why my returnListener is not invoked?
         * <p/>
         * http://rabbitmq.1065348.n5.nabble.com/ReturnListener-is-not-invoked- td24549.html
         * <p/>
         * The call to Channel#waitForConfirmsOrDie() will wait until all messages published since the last call have
         * been either ack'd or nack'd by the broker.
         */
        channel.waitForConfirmsOrDie();
        // now we can close connection
        connection.close();
    }

    private void release(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    public String getHost() {
        return host;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public static void main(String[] argv) throws Exception {
        // RabbitMessagePublishMain main = new
        // RabbitMessagePublishMain("192.168.2.188", EXCHANGE_NAME);
        RabbitMessagePublishMain main = new RabbitMessagePublishMain("localhost", EXCHANGE_NAME);
        main.publish("hello, ramon".getBytes());
        logger.debug(" [x] Published message successfully.");
        // deleteExchange(EXCHANGE_NAME);
    }

    public static void deleteExchange(String exchangeName) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDelete(exchangeName);
        connection.close();
    }

}
