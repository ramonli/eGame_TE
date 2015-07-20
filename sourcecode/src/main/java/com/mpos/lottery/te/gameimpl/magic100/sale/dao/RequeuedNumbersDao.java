package com.mpos.lottery.te.gameimpl.magic100.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbers;

import java.util.List;

public interface RequeuedNumbersDao extends DAO {

    List<RequeuedNumbers> findByGameInstanceAndCountOfValidNumber(String gameInstanceId, int countOfValidNumber);

    RequeuedNumbers findByTransaction(String cancelTransactionId);
}
