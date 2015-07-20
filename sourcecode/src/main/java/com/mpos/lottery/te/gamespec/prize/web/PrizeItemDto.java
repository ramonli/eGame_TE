package com.mpos.lottery.te.gamespec.prize.web;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * The prize information of a given ticket in a single game instance.
 * 
 * @author Ramon Li
 */
public class PrizeItemDto implements java.io.Serializable {
    private static final long serialVersionUID = 8487594818103123875L;
    /**
     * Refer to definition of {@code PrizeGroupItem#GROUP_TYPE_NORMAL_DRAW}
     */
    private int type = PrizeGroupItem.GROUP_TYPE_NORMAL_DRAW;
    /**
     * the prize information of this given game instance.
     */
    private BaseGameInstance gameInstance;
    /**
     * The detailed winning information
     */
    private List<PrizeLevelItemDto> prizeLevelItems = new LinkedList<PrizeLevelItemDto>();
    private List<? extends BaseWinningItem> winningItems = new LinkedList<BaseWinningItem>();

    // to sum the amount of all child PrizeLevelItemDtos
    /**
     * The cash prize amount(before tax).
     */
    private BigDecimal prizeAmount = new BigDecimal("0");
    /**
     * The cash tax amount of prize.
     */
    private BigDecimal taxAmount = new BigDecimal("0");
    /**
     * cash actualAmount := prizeAmount - taxAmount
     */
    private BigDecimal actualAmount = new BigDecimal("0");
    /**
     * The total prize amount of object, just for reference.
     */
    private BigDecimal objectPrizeAmount = new BigDecimal("0");
    /**
     * The total tax amount of object, just for reference
     */
    private BigDecimal objectTaxAmount = new BigDecimal("0");

    public BaseGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public List<PrizeLevelItemDto> getPrizeLevelItems() {
        return prizeLevelItems;
    }

    public void setPrizeLevelItems(List<PrizeLevelItemDto> prizeLevelItems) {
        this.prizeLevelItems = prizeLevelItems;
    }

    public List<? extends BaseWinningItem> getWinningItems() {
        return winningItems;
    }

    public void setWinningItems(List<? extends BaseWinningItem> winningItems) throws ApplicationException {
        this.winningItems = winningItems;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public BigDecimal getObjectPrizeAmount() {
        return objectPrizeAmount;
    }

    public void setObjectPrizeAmount(BigDecimal objectPrizeAmount) {
        this.objectPrizeAmount = objectPrizeAmount;
    }

    public BigDecimal getObjectTaxAmount() {
        return objectTaxAmount;
    }

    public void setObjectTaxAmount(BigDecimal objectTaxAmount) {
        this.objectTaxAmount = objectTaxAmount;
    }

    public PrizeLevelItemDto lookupPrizeLevelItem(int prizeLevel, boolean newIfNull) {
        PrizeLevelItemDto prizeLevelItem = null;
        for (PrizeLevelItemDto item : this.getPrizeLevelItems()) {
            if ((prizeLevel + "").equals(item.getPrizeLevel())) {
                prizeLevelItem = item;
            }
        }
        if (newIfNull && prizeLevelItem == null) {
            prizeLevelItem = new PrizeLevelItemDto();
            prizeLevelItem.setPrizeLevel(prizeLevel + "");
            this.getPrizeLevelItems().add(prizeLevelItem);
        }
        return prizeLevelItem;
    }

    // public BigDecimal calPrizeAmount() {
    // BigDecimal prizeAmount = new BigDecimal("0");
    // for (PrizeLevelItemDto levelItem : this.getPrizeLevelItems()) {
    // prizeAmount = prizeAmount.add(levelItem.getPrizeAmount().multiply(
    // new BigDecimal(levelItem.getNumberOfPrizeLevel())));
    // }
    //
    // return prizeAmount;
    // }
    //
    // public BigDecimal calActualAmount() {
    // BigDecimal actualAmount = new BigDecimal("0");
    // for (PrizeLevelItemDto levelItem : this.getPrizeLevelItems()) {
    // actualAmount = actualAmount.add(levelItem.getActualAmount().multiply(
    // new BigDecimal(levelItem.getNumberOfPrizeLevel())));
    // }
    //
    // return actualAmount;
    // }
    //
    // public BigDecimal calTaxAmount() {
    // BigDecimal taxAmount = new BigDecimal("0");
    // for (PrizeLevelItemDto levelItem : this.getPrizeLevelItems()) {
    // taxAmount = taxAmount.add(levelItem.getTaxAmount().multiply(
    // new BigDecimal(levelItem.getNumberOfPrizeLevel())));
    // }
    //
    // return taxAmount;
    // }
}
