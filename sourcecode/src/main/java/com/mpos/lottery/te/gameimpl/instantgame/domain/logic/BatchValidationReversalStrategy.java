package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.common.util.JSONHelper;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class BatchValidationReversalStrategy extends ValidateReversalStrategy {

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.BATCH_VALIDATION.getRequestType());
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction trans) throws ApplicationException {
        // get tickets inforamtion
        String reqMsg = trans.getTransMessage().getRequestMsg();
        if (reqMsg == null) {
            throw new SystemException("can NOT find request message by transaction_id=" + trans.getId() + ".");
        }
        JSONObject reqModel = (JSONObject) JSONHelper.decode(reqMsg);
        Iterator<String> iterator = reqModel.keySet().iterator();
        BigDecimal credit = new BigDecimal("0");
        while (iterator.hasNext()) {
            String serialNo = iterator.next();
            long errorCode = (Long) reqModel.get(serialNo);
            if (SystemException.CODE_OK != (int) errorCode) {
                continue;
            }

            InstantTicket ticket = this.getInstantTicketDao().getBySerialNo(serialNo);
            if (ticket == null) {
                throw new ApplicationException(SystemException.CODE_NO_TICKET,
                        "can NOT find instant ticket with serialNo='" + serialNo + "'.");
            }
            credit = credit.add(this.reverseBatchPayout(trans, serialNo));
            this.reverseTicket(ticket);
            this.reverseVIRN(ticket);
        }
        // restore credit limit.
        this.getCreditService().credit(trans.getOperatorId(), trans.getMerchantId(), credit,
                respCtx.getTransaction().getGameId(), false, false, false);
        return false;
    }

    /**
     * Due to table 'payout' doesn't aware of game, all payouts, including lotto, ig, horse racing, etc will be stored
     * in this table, the ticket seiral number maybe multipled(different game). To batch validation, one transaction
     * will associate with many payout records, so we have to use transaction and ticket to locate a payout record.
     */
    private BigDecimal reverseBatchPayout(Transaction trans, String ticketSerialNo) {
        List<Payout> payouts = this.getPayoutDao().getByTransactionAndTicketAndStatus(trans.getId(), ticketSerialNo,
                Payout.STATUS_PAID);
        BigDecimal tmpCredit = new BigDecimal("0");
        for (Payout payout : payouts) {
            tmpCredit = tmpCredit.add(payout.getTotalAmount());
            payout.setStatus(Payout.STATUS_REVERSED);
            this.getPayoutDao().update(payout);
        }
        return tmpCredit;
    }

}
