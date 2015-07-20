package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;

import org.springframework.dao.DataAccessException;

public interface VatSaleTransactionDao extends DAO {

    VatSaleTransaction findByTransaction(String teTransId) throws DataAccessException;

    VatSaleTransaction findBySerialnoAndOperatorid(String serialno, String operatorid) throws DataAccessException;

    VatSaleTransaction findByRefNo(String refNo) throws DataAccessException;
}
