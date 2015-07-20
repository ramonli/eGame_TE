package com.mpos.lottery.te.trans;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class TransactionController {
    @Resource(name = "transService")
    private TransactionService transactionService;

    @RequestMap("{transType:310}")
    public void enquiry(Context request, Context response) throws ApplicationException {
        Transaction targetTrans = (Transaction) request.getModel();
        Transaction dbTrans = this.getTransactionService().enquiry(response, targetTrans.getDeviceId(),
                targetTrans.getTraceMessageId());
        response.setModel(dbTrans);
    }

    @RequestMap("{transType:206}")
    public void cancelByTrans(Context request, Context response) throws ApplicationException {
        Transaction targetTrans = (Transaction) request.getModel();
        boolean isCancelDecline = this.getTransactionService().reverseOrCancel(response, targetTrans);

        if (isCancelDecline) {
            response.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
    }

    @RequestMap("{transType:303}")
    public void reverse(Context request, Context response) throws ApplicationException {
        Transaction targetTrans = (Transaction) request.getModel();
        this.getTransactionService().reverseOrCancel(response, targetTrans);
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

}
