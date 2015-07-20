package com.mpos.lottery.te.gameimpl.extraball.prize.web;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * The prize information of a given ticket in a single game instance.
 * 
 * @author Ramon Li
 */
public class PrizeItem implements java.io.Serializable {
    private static final long serialVersionUID = 8487594818103123875L;
    /**
     * If payout mode is refund, the sum of future game instance should be returned to customer.
     */
    private BigDecimal returnAmount = new BigDecimal("0");
    /**
     * The prize amount(before tax).
     */
    private BigDecimal prizeAmount = new BigDecimal("0");
    /**
     * The tax amount of prize.
     */
    private BigDecimal taxAmount = new BigDecimal("0");
    /**
     * actualAmount := prizeAmount + returnAmount - taxAmount.
     */
    private BigDecimal actualAmount = new BigDecimal("0");
    /**
     * the prize information of this given game instance.
     */
    private BaseGameInstance gameInstance;
    /**
     * The detailed winning information.
     */
    private List<BaseWinningItem> winningItems = new LinkedList<BaseWinningItem>();

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BaseGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public List<BaseWinningItem> getWinningItems() {
        return winningItems;
    }

    public void setWinningItems(List<BaseWinningItem> winningItems) {
        this.winningItems = winningItems;
    }

}
