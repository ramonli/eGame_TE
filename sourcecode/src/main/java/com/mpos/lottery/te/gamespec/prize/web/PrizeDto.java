package com.mpos.lottery.te.gamespec.prize.web;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PrizeDto implements java.io.Serializable {
    private static final long serialVersionUID = -5323741083713431721L;
    /**
     * If payout mode is refund, the sum of future game instances should be returned to customer.
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
     * actualAmount := prizeAmount + returnAmount - taxAmount
     */
    private BigDecimal actualAmount = new BigDecimal("0");
    // lucky prize amount
    private BigDecimal luckyPrizeAmount = new BigDecimal("0");
    /**
     * Refer to {@link com.mpos.lottery.te.gamespec.game.BaseOperationParameter#PAYOUTMODE_REFUND}
     */
    private int payoutMode = BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET;
    /**
     * The newly generated tickets which will be persisted to database and used to generate new-print-ticket.
     */
    private List<BaseTicket> generatedTickets = new LinkedList<BaseTicket>();
    /**
     * For a multple-draw ticket, for example 3 draws, and the 1st game instance is payout started, 2nd is active , 3rd
     * is new...here ticket associates with 1st game instance will be categorized into 'paidTickets', while tickets
     * associate with 2nd and 3rd game instance will be categorized into futureTickes, for system may generate new
     * tickets for them('generatedTickets).
     */
    private List<BaseTicket> paidTickets = new LinkedList<BaseTicket>();
    private List<BaseTicket> futureTickets = new LinkedList<BaseTicket>();

    /**
     * The physical ticket which represents the client's request.
     */
    private BaseTicket winningTicket;

    /**
     * The physical ticket which represents the new printed physical ticket.
     */
    private BaseTicket newPrintTicket;
    /**
     * Only one PrizeItemDto associate with a single game instance.
     */
    private List<PrizeItemDto> prizeItems = new LinkedList<PrizeItemDto>();
    /**
     * The game information of winning ticket.
     */
    private Game game;
    // whether need to verify payout PIN
    private boolean verifyPIN;

    public PrizeDto() {
    }

    /**
     * Look {@code PrizeItem} by key of game instance. .
     * 
     * @param keyOfGameInstance
     *            THe key of game instance is generated from 'GameId+drawNo'
     * @return a PrizeItem with given game instance key.
     */
    public PrizeItemDto lookupPrizeItem(String keyOfGameInstance) {
        for (PrizeItemDto prizeItem : this.prizeItems) {
            if (keyOfGameInstance.equals(prizeItem.getGameInstance().getKey())) {
                return prizeItem;
            }
        }
        return null;
    }

    public Integer[] getWinnedPrizeLevels() {
        List<Integer> uniquePrizeLevels = new ArrayList<Integer>();
        for (PrizeItemDto prizeItem : this.getPrizeItems()) {
            for (PrizeLevelItemDto w : prizeItem.getPrizeLevelItems()) {
                Integer prizeLevel = Integer.parseInt(w.getPrizeLevel());
                if (!uniquePrizeLevels.contains(prizeLevel)) {
                    uniquePrizeLevels.add(prizeLevel);
                }
            }
        }
        Integer[] i = new Integer[uniquePrizeLevels.size()];
        uniquePrizeLevels.toArray(i);
        return i;
    }

    public void addPrizeItem(PrizeItemDto prizeItem) {
        this.prizeItems.add(prizeItem);
    }

    public Iterator<PrizeItemDto> iterator() {
        return this.prizeItems.iterator();
    }

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

    public int getPayoutMode() {
        return payoutMode;
    }

    public void setPayoutMode(int payoutMode) {
        this.payoutMode = payoutMode;
    }

    public List<BaseTicket> getGeneratedTickets() {
        return generatedTickets;
    }

    public void setGeneratedTickets(List<BaseTicket> generatedTickets) {
        this.generatedTickets = generatedTickets;
    }

    public BaseTicket getWinningTicket() {
        return winningTicket;
    }

    public void setWinningTicket(BaseTicket winningTicket) {
        this.winningTicket = winningTicket;
    }

    public List<BaseTicket> getPaidTickets() {
        return paidTickets;
    }

    public void setPaidTickets(List<BaseTicket> paidTickets) {
        this.paidTickets = paidTickets;
    }

    public List<BaseTicket> getFutureTickets() {
        return futureTickets;
    }

    public void setFutureTickets(List<BaseTicket> futureTickets) {
        this.futureTickets = futureTickets;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isVerifyPIN() {
        return verifyPIN;
    }

    public void setVerifyPIN(boolean verifyPin) {
        this.verifyPIN = verifyPin;
    }

    public List<PrizeItemDto> getPrizeItems() {
        return prizeItems;
    }

    public void setPrizeItems(List<PrizeItemDto> prizeItems) {
        this.prizeItems = prizeItems;
    }

    public BigDecimal getLuckyPrizeAmount() {
        return luckyPrizeAmount;
    }

    public void setLuckyPrizeAmount(BigDecimal luckyPrizeAmount) {
        this.luckyPrizeAmount = luckyPrizeAmount;
    }

    public BaseTicket getNewPrintTicket() {
        return newPrintTicket;
    }

    public void setNewPrintTicket(BaseTicket newPrintTicket) {
        this.newPrintTicket = newPrintTicket;
    }

}
