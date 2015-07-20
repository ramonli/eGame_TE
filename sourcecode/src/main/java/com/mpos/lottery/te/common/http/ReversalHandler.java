package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

import java.io.IOException;
import java.net.URI;

public interface ReversalHandler<T> {
    /**
     * Reverse a request represented by <code>originalMsg</code>
     * 
     * @param uri
     *            THe URI which will handle the reversal request.
     * @param orignalMsg
     *            The message which will be reversed.
     */
    T reverse(URI uri, Message orignalMsg) throws IOException;

}
