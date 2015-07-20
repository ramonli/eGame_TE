package com.mpos.lottery.te.gameimpl.magic100;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gamespec.prize.service.PrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.springframework.stereotype.Controller;

import java.util.List;

import javax.annotation.Resource;

@Controller
public class Magic100Controller {
    @Resource(name = "magic100SaleService")
    private CompositeTicketService ticketService;

    @Resource(name = "magic100PrizeService")
    private PrizeService prizeService;

    @Resource(name = "transService")
    private TransactionService transactionService;

    @RequestMap("{transType:200,gameType:18}")
    public void sell(Context request, Context response) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) request.getModel();
        ticket.setTransaction(request.getTransaction());

        List<? extends BaseTicket> tickets = this.getTicketService().sell(response, ticket);

        // update transaction
        request.getTransaction().setGameId(ticket.getGameInstance().getGameId());
        request.getTransaction().setTotalAmount(ticket.getTotalAmount());
        request.getTransaction().setTicketSerialNo(tickets.get(0).getSerialNo());

        response.setModel(tickets.get(0));
    }

    @RequestMap("{transType:201,gameType:18}")
    public void cancelByTicket(Context request, Context response) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) request.getModel();
        this.getTransactionService().reverseOrCancel(response, ticket);
    }

    @RequestMap("{transType:202,gameType:18}")
    public void enquiry(Context request, Context response) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) request.getModel();
        ticket = (Magic100Ticket) this.getTicketService().enquiry(response, ticket, true);

        response.setModel(ticket);
    }

    @RequestMap("{transType:302,gameType:18}")
    public void payout(Context request, Context response) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        PrizeDto dto = this.getPrizeService().payout(response, ticket);

        response.setModel(dto);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        request.getTransaction().setTotalAmount(dto.getActualAmount());
    }

    @RequestMap("{transType:301,gameType:18}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        PrizeDto prize = this.getPrizeService().enquiry(response, ticket);

        if (prize != null) {
            /**
             * Refer to http://www.mail-archive.com/castor-user@exolab.org/msg00487.html, you can't define a super class
             * in Castor mapping file and expect castor generate correct xml from a instance of sub-class.
             */
            for (PrizeItemDto prizeItem : prize.getPrizeItems()) {
                prizeItem.setGameInstance(prizeItem.getGameInstance().genCastorBaseGameInstance());
            }
        }

        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        response.setModel(prize);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public CompositeTicketService getTicketService() {
        return ticketService;
    }

    public void setTicketService(CompositeTicketService ticketService) {
        this.ticketService = ticketService;
    }

    public PrizeService getPrizeService() {
        return prizeService;
    }

    public void setPrizeService(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

}
