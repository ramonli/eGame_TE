package com.mpos.lottery.te.gameimpl.raffle;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
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
public class RaffleController {
    @Resource(name = "raffleTicketService")
    private CompositeTicketService ticketService;
    @Resource(name = "rafflePrizeService")
    private PrizeService prizeService;
    @Resource(name = "transService")
    private TransactionService transactionService;

    @RequestMap("{transType:200,gameType:14}")
    public void sell(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());

        List<? extends BaseTicket> tickets = this.getTicketService().sell(response, ticket);

        // update transaction
        request.getTransaction().setGameId(ticket.getGameInstance().getGame().getId());
        request.getTransaction().setTotalAmount(ticket.getTotalAmount());
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());

        response.setModel(ticket);
    }

    @RequestMap("{transType:201,gameType:14}")
    public void cancelByTicket(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
        // boolean isCancelDecline = this.getTicketService().cancelByTicket(response, ticket);
        boolean isCancelDecline = this.getTransactionService().reverseOrCancel(response, ticket);

        if (isCancelDecline) {
            response.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
    }

    @RequestMap("{transType:202,gameType:14}")
    public void enquiryTicket(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
        ticket = (RaffleTicket) this.getTicketService().enquiry(response, ticket, false);

        response.setModel(ticket);
    }

    @RequestMap("{transType:301,gameType:14}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
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

    @RequestMap("{transType:302,gameType:14}")
    public void payout(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        PrizeDto dto = this.getPrizeService().payout(response, ticket);

        response.setModel(dto);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        request.getTransaction().setTotalAmount(dto.getActualAmount());
    }

    @RequestMap("{transType:205,gameType:14}")
    public void confirmPayout(Context request, Context response) throws ApplicationException {
        RaffleTicket ticket = (RaffleTicket) request.getModel();
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        this.prizeService.confirmPayout(response, ticket);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    /**
     * Construct a <code>BaseGameInstance</code> based on DTO.
     */
    protected RaffleGameInstance assembleGameInstance(GameInstanceDto dto) {
        RaffleGameInstance gameInstance = new RaffleGameInstance();
        gameInstance.setNumber(dto.getNumber());
        Game game = new Game();
        game.setId(dto.getGameId());
        gameInstance.setGame(game);
        return gameInstance;
    }

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
