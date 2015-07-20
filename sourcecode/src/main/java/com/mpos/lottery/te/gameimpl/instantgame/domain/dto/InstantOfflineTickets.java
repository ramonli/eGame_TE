package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.util.List;

public class InstantOfflineTickets implements java.io.Serializable {
    private Transaction transaction;
    private List<InstantTicket> tickets;

    public List<InstantTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<InstantTicket> tickets) {
        this.tickets = tickets;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

}
