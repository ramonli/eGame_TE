package com.mpos.lottery.te.gameimpl.digital;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.prize.service.PrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.springframework.stereotype.Controller;

import java.util.List;

import javax.annotation.Resource;

@Controller
public class DigitalController {
    @Resource(name = "digitalPrizeService")
    private PrizeService prizeService;
    @Resource(name = "digitalSaleService")
    private CompositeTicketService ticketService;
    @Resource(name = "transService")
    private TransactionService transactionService;

    @RequestMap("{transType:200,gameType:7}")
    public void sell(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());

        List<? extends BaseTicket> tickets = this.getTicketService().sell(response, ticket);
        response.setModel(ticket);
    }

    @RequestMap("{transType:201,gameType:7}")
    public void cancelByTicket(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();
        // boolean isCancelDecline =
        // this.getTicketService().cancelByTicket(response, ticket);
        boolean isCancelDecline = this.getTransactionService().reverseOrCancel(response, ticket);

        if (isCancelDecline) {
            response.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
    }

    @RequestMap("{transType:202,gameType:7}")
    public void enquiry(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();
        ticket = (DigitalTicket) this.getTicketService().enquiry(response, ticket, true);

        response.setModel(ticket);
    }

    @RequestMap("{transType:211,gameType:7}")
    public void enquiryQP(Context request, Context response) throws ApplicationException {
        QPEnquiryDto dto = (QPEnquiryDto) request.getModel();
        dto = this.getTicketService().enquiryQP(response, dto);
        response.setModel(dto);
    }

    @RequestMap("{transType:301,gameType:7}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();

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

        // configure DTO
        if (BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET == prize.getPayoutMode()) {
            prize.setReturnAmount(null);
        }
        // /**
        // * NOTE: as 'winningTicket' is a JPA runtime managed entity, any
        // * modification applied to it will be committed automatically, Be
        // * careful!
        // */
        // BaseTicket winningTicket = prize.getWinningTicket();
        // winningTicket = (BaseTicket) winningTicket.clone();
        // winningTicket.setTotalAmount(winningTicket.getTotalAmount().multiply(
        // new BigDecimal(winningTicket.getMultipleDraws())));
        // prize.setWinningTicket(winningTicket);

        response.setModel(prize);
    }

    @RequestMap("{transType:302,gameType:7}")
    public void payout(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();

        PrizeDto prize = this.getPrizeService().payout(response, ticket);

        // configure DTO
        if (BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET == prize.getPayoutMode()) {
            prize.setReturnAmount(null);
        }

        response.setModel(prize);
    }

    @RequestMap("{transType:205,gameType:7}")
    public void confirmPayout(Context request, Context response) throws ApplicationException {
        DigitalTicket ticket = (DigitalTicket) request.getModel();
        this.getPrizeService().confirmPayout(response, ticket);
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
