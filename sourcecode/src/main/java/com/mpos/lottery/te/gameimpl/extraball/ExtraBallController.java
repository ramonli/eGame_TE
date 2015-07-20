package com.mpos.lottery.te.gameimpl.extraball;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.extraball.prize.service.PrizeService;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class ExtraBallController {
    @Resource(name = "extraballPrizeService")
    private PrizeService prizeService;
    @Resource(name = "extraballTicketService")
    private CompositeTicketService ticketService;

    @RequestMap("{transType:200,gameType:13}")
    public void sell(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        this.getTicketService().sell(response, ticket);
        // assemble transaction
        request.getTransaction().setTotalAmount(ticket.getTotalAmount());
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        response.setModel(ticket);
    }

    @RequestMap("{transType:201,gameType:13}")
    public void cancelByTicket(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        response.setTransaction(request.getTransaction());
        if (ticket.isManualCancel()) {
            response.getTransaction().setType(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType());
        }
        boolean isCancelDeclined = this.getTicketService().cancelByTicket(response, ticket);

        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        if (isCancelDeclined) {
            response.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
    }

    @RequestMap("{transType:202,gameType:13}")
    public void enquiryTicket(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();
        ticket = (ExtraBallTicket) this.getTicketService().enquiry(response, ticket, false);

        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        request.getTransaction().setTotalAmount(ticket.getTotalAmount());
        response.setModel(ticket);
    }

    @RequestMap("{transType:301,gameType:13}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();
        Prize prize = this.getPrizeService().enquiry(response, ticket);

        response.setModel(prize);
    }

    @RequestMap("{transType:302,gameType:13}")
    public void payout(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();

        Prize prize = this.getPrizeService().payout(response, ticket);
        response.setModel(prize);
    }

    @RequestMap("{transType:205,gameType:13}")
    public void confirmPayout(Context request, Context response) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) request.getModel();
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        this.prizeService.confirmPayout(response, ticket);
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

}
