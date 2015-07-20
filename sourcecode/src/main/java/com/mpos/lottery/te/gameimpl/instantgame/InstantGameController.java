package com.mpos.lottery.te.gameimpl.instantgame;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.common.util.JSONHelper;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ConfirmBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTicketResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTickets;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.Packet;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.service.InstantOperatorService;
import com.mpos.lottery.te.gameimpl.instantgame.service.InstantTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@Controller
public class InstantGameController {
    @Resource(name = "igTicketService")
    private InstantTicketService ticketService;

    @Resource(name = "igOperatorService")
    private InstantOperatorService igOperatorService;

    @RequestMap("{transType:401}")
    public void activeByCriteria(Context request, Context response) throws ApplicationException {
        ActiveCriteria criteria = (ActiveCriteria) request.getModel();
        criteria.setTrans(request.getTransaction());
        ActiveResult result = this.getTicketService().active(criteria);
        if (result.getCount() == 0 && !result.isBatchSuccessful()) {
            // only fail to active all tickets, this response code should be
            // returned
            response.setResponseCode(SystemException.CODE_FAIL_ACTIVEBYCRITERIA);
        } else {
            response.setResponseCode(SystemException.CODE_OK);
        }

        // save criteria to transMessage
        TransactionMessage transMessage = new TransactionMessage();
        transMessage.setTransactionId(request.getTransaction().getId());
        transMessage.setRequestMsg(result.getActiveResult());
        request.getTransaction().setTransMessage(transMessage);
        response.setModel(result);
    }

    @RequestMap("{transType:402}")
    public void validateInstantTicket(Context request, Context response) throws ApplicationException {
        PrizeLevelDto payout = (PrizeLevelDto) request.getModel();
        payout.getTicket().setTransaction(request.getTransaction());
        PrizeLevelDto dto = this.getTicketService().validate(response, payout);
        response.setModel(dto);
        // response.setResponseCode(dto.getErrorCode());

        request.getTransaction().setTicketSerialNo(payout.getTicket().getSerialNo());
        // tran.setVirn(payout.getTicket().getTicketXOR2());
        request.getTransaction().setTotalAmount(dto.getCashActualAmount());
    }

    @RequestMap("{transType:403}")
    public void offlineInfoUpload(Context request, Context response) throws ApplicationException {
        InstantOfflineTickets ticket = (InstantOfflineTickets) request.getModel();
        InstantOfflineTicketResult result = this.getTicketService().offlineInfoUpload(ticket);
        response.setModel(result);
    }

    @RequestMap("{transType:404}")
    public void sellInstantTicket(Context request, Context response) throws ApplicationException {
        InstantTicket ticket = (InstantTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        InstantTicket result = this.getTicketService().sell(ticket);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        request.getTransaction().setTotalAmount(result.getTotalAmount());
        response.setModel(result);
    }

    @RequestMap("{transType:405}")
    public void receivePacket(Context request, Context response) throws ApplicationException {
        Packet packet = (Packet) request.getModel();
        this.getTicketService().receive(packet);
    }

    @RequestMap("{transType:406}")
    public void activeTicket(Context request, Context response) throws ApplicationException {
        InstantTicket ticket = (InstantTicket) request.getModel();
        ticket.setTransaction(request.getTransaction());
        InstantTicket result = this.getTicketService().active(ticket);
        request.getTransaction().setTicketSerialNo(ticket.getSerialNo());
        response.setModel(result);
    }

    @RequestMap("{transType:407}")
    public void batchValidate(Context request, Context response) throws ApplicationException {
        InstantBatchPayoutDto batchPayout = (InstantBatchPayoutDto) request.getModel();
        List<PrizeLevelDto> payoutReq = batchPayout.getPayouts();
        for (PrizeLevelDto dto : payoutReq) {
            // set transaction to InstantTicket
            dto.getTicket().setTransaction(request.getTransaction());
        }

        InstantBatchPayoutDto result = this.getTicketService().batchValidate(response, batchPayout);
        response.setModel(result);
        // fail to validate any ticket
        if (result.getTotalFail() == result.getPayouts().size()) {
            response.setResponseCode(SystemException.CODE_FAIL_BATCHVALIDATION);
        }
        request.getTransaction().setTotalAmount(result.getActualAmount());

        // write all instant tickets to transaction_message for reversal
        Map ticketResultMap = new HashMap();
        List<PrizeLevelDto> payouts = batchPayout.getPayouts();
        for (PrizeLevelDto dto : payouts) {
            ticketResultMap.put(dto.getTicket().getSerialNo(), dto.getErrorCode());
        }
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(request.getTransaction().getId());
        transMsg.setRequestMsg(JSONHelper.encode(ticketResultMap));
        request.getTransaction().setTransMessage(transMsg);
    }

    /**
     * need to handle confirmation batch validation transaction.
     * */
    @RequestMap("{transType:410}")
    public void patialPackage(Context request, Context response) throws ApplicationException {
        ConfirmBatchPayoutDto confirmBatchPayout = (ConfirmBatchPayoutDto) request.getModel();
        List<PrizeLevelDto> payoutReq = confirmBatchPayout.getPayouts();
        for (PrizeLevelDto dto : payoutReq) {
            // set transaction to InstantTicket
            dto.getTicket().setTransaction(request.getTransaction());
        }

        ConfirmBatchPayoutDto result = this.getTicketService().partialPackage(response, confirmBatchPayout);

        // fail to validate any ticket
        if (result.getTotalFail() == result.getPayouts().size()) {
            // response.setResponseCode(SystemException.CODE_FAIL_BATCHVALIDATION);
        }
        response.setModel(null);
    }

    /**
     * need to handle confirmation batch validation transaction.
     * */
    @RequestMap("{transType:411}")
    public void ConfirmBatchValidation(Context request, Context response) throws ApplicationException {
        InstantBatchReportDto dto = (InstantBatchReportDto) request.getModel();

        InstantBatchReportDto result = this.getTicketService().ConfirmBatchValidation(response, dto);

        // fail to validate any ticket
        if (result == null || result.getBatchNumber() == 0) {
            // response.setResponseCode(SystemException.CODE_FAIL_BATCHVALIDATION);
        }
        response.setModel(result);
    }

    @RequestMap("{transType:408}")
    public void enquiryPrize(Context request, Context response) throws ApplicationException {
        InstantTicket dto = (InstantTicket) request.getModel();
        PrizeLevelDto prize = this.getTicketService().enquiryPrize(response, dto, false);
        response.setModel(prize);
    }

    @RequestMap("{transType:409}")
    public void GetConfirmBatchNumber(Context request, Context response) throws ApplicationException {
        InstantBatchReportDto instantBatchReportDto = this.getIgOperatorService().getConfirmBatchNumber(request);
        response.setModel(instantBatchReportDto);
    }

    @RequestMap("{transType:312}")
    public void offlineBatchValidate(Context request, Context response) throws ApplicationException {
        InstantBatchPayoutDto dto = (InstantBatchPayoutDto) request.getModel();
        request.setTransaction(request.getTransaction());
        InstantBatchPayoutDto resp = this.getTicketService().offlineBatchValidate(response, dto);
        response.setModel(resp);
    }

    @RequestMap("{transType:412}")
    public void getReportOfConfirmBatchValidation(Context request, Context response) throws ApplicationException {

        InstantBatchReportDto dto = (InstantBatchReportDto) request.getModel();
        InstantBatchReportDto resp = this.getTicketService().getReportOfConfirmBatchValidation(request, dto);
        response.setModel(resp);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public InstantTicketService getTicketService() {
        return ticketService;
    }

    public void setTicketService(InstantTicketService ticketService) {
        this.ticketService = ticketService;
    }

    public InstantOperatorService getIgOperatorService() {
        return igOperatorService;
    }

    public void setIgOperatorService(InstantOperatorService igOperatorService) {
        this.igOperatorService = igOperatorService;
    }

}
