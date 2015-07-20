package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.google.protobuf.Message;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.util.Assert;

public class MessagePack {
    public static final MessageProperties MSGPROP_PERSISTENT_BASIC = new MessageProperties();
    public static final String PREFIX = "TE";
    public static final String HEADER_TRANSACTION = "TRANSACTION_ID";
    private String exchangeName;
    private String routingKey;
    private Message protobuffMessage;
    private MessageProperties messageProperties;
    private org.springframework.amqp.core.Message amqpMessage;

    static {
        MSGPROP_PERSISTENT_BASIC.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
        MSGPROP_PERSISTENT_BASIC.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        MSGPROP_PERSISTENT_BASIC.setPriority(0);
    }

    /**
     * Constructor by given parameters.
     */
    public MessagePack(String exchangeName, String routingKey, Message protobuffMessage,
            MessageProperties messageProperties) {
        super();

        Assert.hasText(exchangeName);
        Assert.hasText(routingKey);
        Assert.notNull(protobuffMessage);
        Assert.notNull(messageProperties);

        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.protobuffMessage = protobuffMessage;
        this.messageProperties = messageProperties;
        this.assembleAmqpMessage(messageProperties);
    }

    /**
     * construct a persistent message.
     */
    public MessagePack(String exchangeName, String routingKey, Message protobuffMessage) {
        super();

        Assert.hasText(exchangeName);
        Assert.hasText(routingKey);
        Assert.notNull(protobuffMessage);

        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.protobuffMessage = protobuffMessage;

        // set message properties to PERSISTENT_BASIC, refer to
        // http://www.rabbitmq.com/releases/rabbitmq-java-client/v3.2.1/rabbitmq-java-client-javadoc-3.2.1/
        this.messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        // Message priorities have been clarified: priority 9 is the highest
        // priority level (meaning that messages with priority 9 will be
        // delivered ahead of any lower-priority messages), and priority 0 is
        // the lowest priority level.
        // set priority of cancellation transaction to a lower value
        messageProperties.setPriority(5);
        this.assembleAmqpMessage(messageProperties);
    }

    private void assembleAmqpMessage(MessageProperties messageProperties) {
        this.amqpMessage = new org.springframework.amqp.core.Message(protobuffMessage.toByteArray(), messageProperties);
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public org.springframework.amqp.core.Message getAmqpMessage() {
        return this.amqpMessage;
    }

    public Message getProtobuffMessage() {
        return this.protobuffMessage;
    }

    public MessageProperties getMessageProperties() {
        return messageProperties;
    }

}
