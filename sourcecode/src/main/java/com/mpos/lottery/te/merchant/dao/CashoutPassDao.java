package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.CashoutPass;

import org.springframework.dao.DataAccessException;

public interface CashoutPassDao extends DAO {
    CashoutPass findByBarcode(String barcode) throws DataAccessException;

    CashoutPass findByBarcodeAndPassword(String barcode, String password) throws DataAccessException;

    void increaseTriedTimes(String barcode) throws DataAccessException;
}
