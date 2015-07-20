package com.mpos.lottery.te.thirdpartyservice;

import com.mpos.lottery.te.config.exception.SystemException;

public enum PaymentResponseCode {
    OK(200);

    private int code;

    private PaymentResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static PaymentResponseCode from(int code) {
        PaymentResponseCode types[] = PaymentResponseCode.values();
        for (int i = 0; i < types.length; i++) {
            if (code == types[i].getCode()) {
                return types[i];
            }
        }
        throw new SystemException("Unsupported 3rd-party response code:" + code);
    }
}
