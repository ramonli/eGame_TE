package com.mpos.lottery.te.valueaddservice.airtime.support;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.transactionhandle.AbstractTransactionHandle;
import com.mpos.lottery.te.trans.domain.transactionhandle.TransactionHandle;
import com.mpos.lottery.te.valueaddservice.airtime.dao.AirtimeDao;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("airtimeTransactionHandle")
public class AirtimeTransactionHandle extends AbstractTransactionHandle {

    @Resource(name = "jpaAirtimeDao")
    private AirtimeDao airtimeDao;

    /*
     * (non-Jsdoc)
     * 
     * @see com.mpos.lottery.te.trans.TransactionHandle#supportHandle()
     */
    @Override
    public int supportHandle() {
        return GameType.AIRTIME.getType();
    }

    @Override
    public Object getTransactionModel(Context respCtx, Transaction trans) {
        return airtimeDao.getAirtimeTopupByTeTransactionId(trans.getId());
    }

    public AirtimeDao getAirtimeDao() {
        return airtimeDao;
    }

    public void setAirtimeDao(AirtimeDao airtimeDao) {
        this.airtimeDao = airtimeDao;
    }

}
