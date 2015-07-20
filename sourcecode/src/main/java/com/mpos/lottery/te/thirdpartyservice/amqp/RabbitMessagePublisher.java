package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.mpos.lottery.te.config.exception.SystemException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;

import java.io.IOException;

public class RabbitMessagePublisher implements MessagePublisher {
    private Log logger = LogFactory.getLog(RabbitMessagePublisher.class);
    private ObjectPool connectionPool;

    public RabbitMessagePublisher(ObjectPool connPool) {
        this.connectionPool = connPool;
    }

    @Override
    public void publish(final byte[] message, final String exchangeName, final String routingKey) throws IOException {
        this.exchange(new MessageCallback() {

            @Override
            public void doExchange(MessageContext context) throws ShutdownSignalException, IOException {
                Channel channel = context.getChannel();
                // declare a 'topic' type of exchange
                channel.exchangeDeclare(exchangeName, "topic");
                // Content-type "application/octet-stream", deliveryMode 2
                // (persistent), priority zero
                channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_BASIC, message);
                if (logger.isDebugEnabled()) {
                    logger.debug("Publish message[" + message + "] successfully.");
                }
            }
        });
    }

    /**
     * This method will manage the connection pool, and re-connect is remote server is down. The real publishing or
     * consuming is delegated to <code>MessageCallback</code>.
     * 
     * @param callback
     *            THe <code>MessageCallback</code> which will publish or consume message.
     */
    protected void exchange(MessageCallback callback) throws IOException {
        boolean rebuildPool = false;
        try {
            Connection conn = null;
            Channel channel = null;

            conn = (Connection) connectionPool.borrowObject();
            try {
                channel = conn.createChannel();
                // assemble message context
                MessageContext msgContext = new MessageContext();
                msgContext.setChannel(channel);
                callback.doExchange(msgContext);
            } catch (Exception e) {
                // can catch only IOException, for example, if AMQP server is
                // closed, a 'com.rabbitmq.client.AlreadyClosedException' will be
                // thrown out.
                logger.warn(e);
                // if a IOException caught, it basically means the underlying
                // connection is closed, and should be rebuild the connection
                // pool
                rebuildPool = true;

                // If one connection failed, it commonly means all connections
                // in the pool are failed(due to closure of AMQP server). In
                // this case, it is better to clear idle pool, and let
                // subsequent request new a connection
                this.connectionPool.clear();

                // invalidate the connection instance...it will close the
                // connection
                this.connectionPool.invalidateObject(conn);
            } finally {
                if (channel != null) {
                    channel.close();
                }
                if (conn != null && !rebuildPool) {
                    /**
                     * make sure return the object back to pool. However if <code>rebuildPool</code> is true, in which
                     * case current connection is damaged, the connection should be invalidate(essentially closed).
                     */
                    this.connectionPool.returnObject(conn);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Stat of connection pool: " + this.connectionPool.getNumActive() + " active, "
                            + this.connectionPool.getNumIdle() + " idle");
                }
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    public ObjectPool getConnectionPool() {
        return connectionPool;
    }
}
