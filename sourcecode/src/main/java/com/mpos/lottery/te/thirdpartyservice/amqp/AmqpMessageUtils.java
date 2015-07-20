package com.mpos.lottery.te.thirdpartyservice.amqp;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

public class AmqpMessageUtils {

    /**
     * Build a AMQP cancellation message based on <code>Transaction</code> instance.
     */
    public static TeTransactionMessage.Cancellation assembleCancellationMsg(Context<?> respCtx,
            Transaction dbTargetTrans) {
        TeTransactionMessage.Cancellation.Builder cancelTransBuilder = TeTransactionMessage.Cancellation.newBuilder();
        cancelTransBuilder.setCancelTrans(assembleTransactionMsg(respCtx));
        // assemble original trans
        TeTransactionMessage.Transaction.Builder origTransBuilder = TeTransactionMessage.Transaction.newBuilder();
        origTransBuilder.setId(dbTargetTrans.getId()).setDevId(dbTargetTrans.getDeviceId() + "")
                .setOperatorId(dbTargetTrans.getOperatorId()).setMerchantId(dbTargetTrans.getMerchantId() + "")
                .setTransType(dbTargetTrans.getType()).setCreateTime(dbTargetTrans.getCreateTime().getTime())
                .setGameType(respCtx.getGameTypeIdIntValue());
        if (dbTargetTrans.getGameId() != null) {
            origTransBuilder.setGameId(dbTargetTrans.getGameId());
        }
        if (dbTargetTrans.getTotalAmount() != null) {
            origTransBuilder.setTotalAmount(dbTargetTrans.getTotalAmount() + "");
        }
        cancelTransBuilder.setOrigTrans(origTransBuilder.build());

        return cancelTransBuilder.build();
    }

    /**
     * Build a AMQP message based on <code>Transaction</code> instance.
     */
    public static TeTransactionMessage.Transaction assembleTransactionMsg(Context<?> respCtx) {
        TeTransactionMessage.Transaction.Builder builder = TeTransactionMessage.Transaction.newBuilder();
        builder.setId(respCtx.getTransaction().getId()).setDevId(respCtx.getTransaction().getDeviceId() + "")
                .setOperatorId(respCtx.getTransaction().getOperatorId())
                .setMerchantId(respCtx.getTransaction().getMerchantId() + "")
                .setTransType(respCtx.getTransaction().getType())
                .setCreateTime(respCtx.getTransaction().getCreateTime().getTime())
                .setGameType(respCtx.getGameTypeIdIntValue());
        if (respCtx.getTransaction().getGameId() != null) {
            builder.setGameId(respCtx.getTransaction().getGameId());
        }
        if (respCtx.getTransaction().getTotalAmount() != null) {
            builder.setTotalAmount(respCtx.getTransaction().getTotalAmount() + "");
        }
        return builder.build();
    }
}
