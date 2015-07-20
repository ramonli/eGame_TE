package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;

import org.springframework.dao.DataAccessException;

public interface VatOperatorBalanceDao extends DAO {
    VatOperatorBalance findByOperatorIdForUpdate(String operatorid) throws DataAccessException;

    VatOperatorBalance findByOperator(String operatorid) throws DataAccessException;
}
