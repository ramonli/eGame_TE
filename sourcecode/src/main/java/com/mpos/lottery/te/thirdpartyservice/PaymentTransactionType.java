package com.mpos.lottery.te.thirdpartyservice;

import com.mpos.lottery.te.config.exception.SystemException;

public enum PaymentTransactionType {
    CASHOUT(240, false),
    ENQUIRY_ACCOUNT(200, false),
    REVERSAL(280, true),
    ACTIVATE_VOUCHER(270, false),
    TOPUP(210, false);

    private int transType;
    private boolean needRetryIfTimeout;

    private PaymentTransactionType(int transType, boolean needRetryIfTimeout) {
        this.transType = transType;
        this.needRetryIfTimeout = needRetryIfTimeout;
    }

    public boolean isNeedRetryIfTimeout() {
        return this.needRetryIfTimeout;
    }

    public int getTransType() {
        return this.transType;
    }

    public static PaymentTransactionType from(int requestType) {
        PaymentTransactionType types[] = PaymentTransactionType.values();
        for (int i = 0; i < types.length; i++) {
            if (requestType == types[i].getTransType()) {
                return types[i];
            }
        }
        throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE, "Unsupported 3rd-party transaction type:"
                + requestType);
    }
}
