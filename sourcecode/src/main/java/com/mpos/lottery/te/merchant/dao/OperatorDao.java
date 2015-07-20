package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.Operator;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;

public interface OperatorDao extends DAO {

    Operator findByIdForUpdate(String id) throws DataAccessException;

    Operator findByLoginNameForUpdate(String loginName) throws DataAccessException;

    Operator findByLoginName(String loginName) throws DataAccessException;

    /** [1)Commission balance 2)Payout balance 3)Cash-out balance] **/
    void deductBalanceByOperator(BigDecimal commissionDeducted, BigDecimal payoutDeducted, BigDecimal cashoutDeducted,
            String operatorid) throws DataAccessException;

    void deductBalanceByOperatorCancel(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, String operatorid) throws DataAccessException;

    void addCashoutAndCommissionToOperator(BigDecimal cashoutAmount, BigDecimal commissionAmount, String operatorid)
            throws DataAccessException;

    void addCashoutAndCommissionToOperatorCancel(BigDecimal cashoutAmount, BigDecimal commissionAmount,
            String operatorid) throws DataAccessException;

}
