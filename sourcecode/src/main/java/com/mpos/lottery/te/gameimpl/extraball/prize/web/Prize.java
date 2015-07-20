package com.mpos.lottery.te.gameimpl.extraball.prize.web;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Prize implements java.io.Serializable {
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
     * actualAmount := prizeAmount + returnAmount - taxAmount.
     */
    private BigDecimal actualAmount = new BigDecimal("0");
    /**
     * Refer to {@link com.mpos.lottery.te.gamespec.game.BaseOperationParameter#PAYOUTMODE_REFUND}.
     */
    private int payoutMode = BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET;
    // private boolean isVerifyPIN;
    /**
     * The newly generated tickets which need to be returned to customer.
     */
    private List<BaseTicket> generatedTickets = new LinkedList<BaseTicket>();
    /**
     * For a multple-draw ticket, for example 3 draws, and the 1st game instance is payout started, 2nd is active , 3rd
     * is new...here ticket assocites with 1st game instance will be categoried into paidTickets, while tickets
     * assocaite with 2nd and 3rd game instance will be categoried into futureTickes, for system may generate new
     * tickets for them.
     */
    private List<BaseTicket> paidTickets = new LinkedList<BaseTicket>();
    private List<BaseTicket> futureTickets = new LinkedList<BaseTicket>();
    /**
     * The physical ticket wins the prize.
     */
    private BaseTicket winningTicket;
    /**
     * Only one PrizeItemDto associate with a single game instance.
     */
    private List<PrizeItem> prizeItems = new LinkedList<PrizeItem>();
    /**
     * The game information of winning ticket.
     */
    private Game game;

    public Prize() {
    }

    public PrizeItem lookupPrizeItem(String keyOfGameInstance) {
        for (PrizeItem prizeItem : this.prizeItems) {
            if (keyOfGameInstance == prizeItem.getGameInstance().getKey()) {
                return prizeItem;
            }
        }
        return null;
    }

    public void addPrizeItem(PrizeItem prizeItem) {
        this.prizeItems.add(prizeItem);
    }

    public Iterator<PrizeItem> iterator() {
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

}
