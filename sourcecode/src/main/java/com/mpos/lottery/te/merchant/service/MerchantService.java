package com.mpos.lottery.te.merchant.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.web.CashOutByManualDto;
import com.mpos.lottery.te.merchant.web.CashOutByOperatorPassDto;
import com.mpos.lottery.te.merchant.web.CashOutPassDto;
import com.mpos.lottery.te.merchant.web.PayoutLevelAllowRequest;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantService {

    /**
     * When cashout is allowed for current operator, below condition will be checked:
     * <ol>
     * <li>cashout limit</li>
     * <li>whether exceeds the cashout day level</li>
     * </ol>
     * 
     * @param context
     *            The context of cashout transaction.
     * @param actualCashout
     *            The actual amount of cashout.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    public void allowCashout(Context context, BigDecimal actualCashout) throws ApplicationException;

    /**
     * Verify whether a merchant can sale a given game.
     */
    void allowSale(Transaction context, BaseTicket ticket) throws ApplicationException;

    /**
     * Whether a merchant can perform payout, includes:
     * <ol>
     * <li>the merchant's payout limit</li>
     * <li>the allowed prize level of a given game</li>
     * </ol>
     * 
     * @param respCtx
     *            The context of payout transaction.
     * @param game
     *            THe game which will be payout.
     * @param levelAllowRequests
     *            The request DTO contains the requested prize levels. If is null, the prize level check will be
     *            ignored.
     * @param actualPayout
     *            The actual payout amount
     */
    void allowPayout(Context respCtx, Game game, PayoutLevelAllowRequest[] levelAllowRequests, BigDecimal actualPayout)
            throws ApplicationException;

    Merchant getMerchant(long merchantId) throws ApplicationException;

    /**
     * Lookup merchant by operator. A operator can be allocated to only one merchant.
     * 
     * @param operatorId
     *            The id of operator.
     * @param forUpdate
     *            Whether lock the returned merchant entity.
     * @return the merchant which manages the operator.
     */
    Merchant getMerchantByOperator(String operatorId, boolean forUpdate) throws ApplicationException;

    void update(Merchant merchant) throws ApplicationException;

    /**
     * Find all supported game types of given merchant.
     * 
     * @param merchantId
     *            The merchant to which the operator is assigned.
     * @return all supported game types.
     * @throws ApplicationException
     *             when encounter any business related exception.
     */
    List<GameType> supportedGameType(long merchantId) throws ApplicationException;

    /**
     * get cash out password
     */
    CashOutPassDto getCashoutPass(Context respCtx, CashOutPassDto topupDto) throws ApplicationException;

    /**
     * cash out by operator password
     */
    CashOutByOperatorPassDto cashoutOperatorByPass(Context respCtx, Context responseCtx,
            CashOutByOperatorPassDto topupDto) throws ApplicationException;

    /**
     * cash out by operator manual
     */
    CashOutByManualDto cashoutOperatorByManual(Context respCtx, CashOutByManualDto topupDto)
            throws ApplicationException;

}
