package com.mpos.lottery.te.gameimpl.bingo.prize;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class BingoPrizeConfirmationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao ticketDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "payoutDetailDao")
    private PayoutDetailDao payoutDetailDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao entryDao;

    @Test
    public void testConfirmRefund_PrintNewTicket() throws Exception {
        this.printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");

        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
        this.jdbcTemplate.update("delete from ld_winning");

        // 1. make payout
        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // 2. confirm payout
        Context confirmReqCtx = this.getDefaultContext(TransactionType.CONFIRM_PAYOUT.getRequestType(), ticket);
        confirmReqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context confirmRespCtx = this.doPost(this.mockRequest(confirmReqCtx));

        assertEquals(200, confirmRespCtx.getResponseCode());
        // this.setComplete();

        // check ticket status
        List<BingoTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
        assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(BingoTicket.STATUS_INVALID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(BingoTicket.STATUS_INVALID, dbTickets.get(2).getStatus());
    }

    @Test
    public void testConfirmRefund_PrintNewTicket_ByBarcode() throws Exception {
        this.printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");
        ticket.setPIN("PIN-111");

        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
        this.jdbcTemplate.update("delete from ld_winning");

        // 1. make payout
        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // 2. confirm payout
        Context confirmReqCtx = this.getDefaultContext(TransactionType.CONFIRM_PAYOUT.getRequestType(), ticket);
        confirmReqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context confirmRespCtx = this.doPost(this.mockRequest(confirmReqCtx));

        assertEquals(200, confirmRespCtx.getResponseCode());
        // this.setComplete();

        // check ticket status
        Barcoder barcoder = new Barcoder(ticket.getBarcode());
        ticket.setRawSerialNo(barcoder.getSerialNo());
        List<BingoTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
        assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(BingoTicket.STATUS_INVALID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(BingoTicket.STATUS_INVALID, dbTickets.get(2).getStatus());
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public BaseTicketDao getTicketDao() {
        return ticketDao;
    }

    public void setTicketDao(BaseTicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

    public BaseEntryDao getEntryDao() {
        return entryDao;
    }

    public void setEntryDao(BaseEntryDao lottoEntryDao) {
        this.entryDao = lottoEntryDao;
    }

}
