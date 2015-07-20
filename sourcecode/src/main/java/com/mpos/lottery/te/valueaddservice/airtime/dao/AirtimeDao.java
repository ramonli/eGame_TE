package com.mpos.lottery.te.valueaddservice.airtime.dao;

import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;

public interface AirtimeDao {
    /**
     * Query AirtimeTopup info by TeTransactionId.
     * 
     * @param teTransactionId
     *            te_transaction's id
     * @return AirtimeTopup A AIRTIME_SELLING_RECORDS
     */
    public AirtimeTopup getAirtimeTopupByTeTransactionId(String teTransactionId);

}