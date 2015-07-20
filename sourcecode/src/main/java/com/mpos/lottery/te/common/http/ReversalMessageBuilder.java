package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

import net.mpos.apc.entry.Reversal.ReqReversal;

public interface ReversalMessageBuilder<T extends Message> {
    /**
     * Build reversal message based on original message.
     */
    ReqReversal build(T originalMessage);
}
