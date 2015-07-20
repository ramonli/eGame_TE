package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;

public class MessageConnectionPoolablObjectFactory extends BasePoolableObjectFactory<Connection> {
    private Log logger = LogFactory.getLog(MessageConnectionPoolablObjectFactory.class);
    private String host;

    @Override
    public Connection makeObject() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.getHost());
        return factory.newConnection();
    }

    @Override
    public void destroyObject(Connection conn) throws Exception {
        try {
            // close the connection
            conn.close();
        } catch (Exception e) {
            // if the conn is closed already,
            // 'com.rabbitmq.client.AlreadyClosedException' will be thrown out
            // when call 'conn.close()'...here simply ignore this exception
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
