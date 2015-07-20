package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

public class ProtoMessageUtil {

    public static String toString(Message message) {
        return (message + "").replace("\r", " ").replace("\n", ",");
    }
}
