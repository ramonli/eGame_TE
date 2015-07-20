package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.Merchant;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantDao extends DAO {

    Merchant findByIdForUpdate(long id) throws DataAccessException;

    List<Merchant> getByParent(Long parentId);

    Merchant getByCode(String code) throws DataAccessException;

    Merchant findByTaxNo(String taxNo);

    Merchant findDistributeMerchantByMerchantId(long merchantId);

    void deductBalanceByMerchant(BigDecimal commissionDeducted, BigDecimal payoutDeducted, BigDecimal cashoutDeducted,
            long merchantid) throws DataAccessException;

    void deductBalanceByMerchantCancel(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, long merchantid) throws DataAccessException;

    void addCashoutAndCommissionToMerchant(BigDecimal cashoutAmount, BigDecimal commissionAmount, long merchantid)
            throws DataAccessException;

    void addCashoutAndCommissionToMerchantCancel(BigDecimal cashoutAmount, BigDecimal commissionAmount, long merchantid)
            throws DataAccessException;
}
