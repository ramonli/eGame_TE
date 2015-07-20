package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.google.protobuf.Message;

import com.mpos.lottery.te.port.Context;

public interface TeTransactionMessageSerializer {
    /**
     * Convert a POJO into protocol buffer Message instance.
     * 
     * @return the protocol buffer Message.
     */
    Message toProtoMessage(Context respCtx);
}
