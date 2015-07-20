package com.mpos.lottery.te.gameimpl.lotto;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.prize.service.PrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class LottoController {
    @Resource(name = "lottoPrizeService")
    private PrizeService prizeService;
    @Resource(name = "lottoSaleService")
    private CompositeTicketService ticketService;
    @Resource(name = "transService")
    private TransactionService transactionService;

    @RequestMap("{transType:200,gameType:1}")
    public void sell(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        // check operator
        this.getTicketService().sell(response, ticket);
        // assemble transaction
        response.setModel(ticket);
    }

    @RequestMap("{transType:201,gameType:1}")
    public void cancelByTicket(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
        // ticket.setTransaction(request.getTransaction());
        // boolean isCancelDecline = this.getTicketService().cancelByTicket(response, ticket);

        boolean isCancelDecline = this.getTransactionService().reverseOrCancel(response, ticket);

        if (isCancelDecline) {
            response.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
    }

    @RequestMap("{transType:202,gameType:1}")
    public void enquiryTicket(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
        ticket = (LottoTicket) this.getTicketService().enquiry(response, ticket, true);

        response.setModel(ticket);
    }

    @RequestMap("{transType:301,gameType:1}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
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

    @RequestMap("{transType:302,gameType:1}")
    public void payout(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        PrizeDto dto = this.getPrizeService().payout(response, ticket);

        response.setModel(dto);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        request.getTransaction().setTotalAmount(dto.getActualAmount());
    }

    @RequestMap("{transType:205,gameType:1}")
    public void confirmPayout(Context request, Context response) throws ApplicationException {
        LottoTicket ticket = (LottoTicket) request.getModel();
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        this.prizeService.confirmPayout(response, ticket);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
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
