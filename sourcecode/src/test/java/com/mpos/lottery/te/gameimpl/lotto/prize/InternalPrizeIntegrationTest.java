package com.mpos.lottery.te.gameimpl.lotto.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseInternalServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import javax.annotation.Resource;

public class InternalPrizeIntegrationTest extends BaseInternalServletIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao ticketDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "payoutDetailDao")
    private PayoutDetailDao payoutDetailDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "newPrintTicketDao")
    private NewPrintTicketDao newprintTicketDao;

    @Rollback(true)
    @Test
    public void testPayout_Accepted_LastDrawIsntPayoutStarted_PrintNewTicket() throws Exception {
        printMethod();
        LottoTicket ticket = new LottoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, respCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testPayout_Accepted_LastDrawPayoutStarted_PrintNewTicket() throws Exception {
        printMethod();
        LottoTicket ticket = new LottoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("update game_instance set STATUS=" + BaseGameInstance.STATE_PAYOUT_STARTED);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_INVALID_PAYOUT, respCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testPayout_CancelDeclined_PrintNewTicket_OK() throws Exception {
        printMethod();
        LottoTicket ticket = new LottoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("update game_instance set STATUS=" + BaseGameInstance.STATE_PAYOUT_STARTED);
        this.jdbcTemplate.update("update te_ticket set status=" + BaseTicket.STATUS_CANCEL_DECLINED);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        LottoTicket newGenTicket = (LottoTicket) dto.getNewPrintTicket();
        assertNull(newGenTicket);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-111");
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(respCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(respCtx.getTerminalId());
        expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectedTrans.setType(reqCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // check ticket status
        List<LottoTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(LottoTicket.class, ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, dbTickets.get(0).getStatus());
        assertEquals(200, dbTickets.get(0).getTransType());
        assertTrue(dbTickets.get(0).isAbsorptionPaid());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, dbTickets.get(1).getStatus());
        assertTrue(dbTickets.get(1).isAbsorptionPaid());
        assertEquals(200, dbTickets.get(1).getTransType());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, dbTickets.get(2).getStatus());
        assertTrue(dbTickets.get(2).isAbsorptionPaid());
        assertEquals(200, dbTickets.get(2).getTransType());

        // assert newprint_ticket
        NewPrintTicket newPrintTicket = this.getNewprintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertNull(newPrintTicket);

        // check payout records
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        Payout dbNormalPayout0 = this.findPayoutByGameInstance("GII-111", payouts);
        assertEquals(5032000.0, dbNormalPayout0.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(4027967.0, dbNormalPayout0.getTotalAmount().doubleValue(), 0);

        Payout dbNormalPayout1 = this.findPayoutByGameInstance("GII-112", payouts);
        assertEquals(2200000.0, dbNormalPayout1.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(860006.0, dbNormalPayout1.getTotalAmount().doubleValue(), 0);

        // verify payout details
        List<PayoutDetail> normalDetails0 = this.getPayoutDetailDao().findByPayout(dbNormalPayout0.getId());
        assertEquals(1, normalDetails0.size());
        PayoutDetail normalDetail0 = normalDetails0.get(0);
        assertEquals(5032000.0, normalDetail0.getPrizeAmount().doubleValue(), 0);
        assertEquals(4027967.0, normalDetail0.getActualAmount().doubleValue(), 0);

        List<PayoutDetail> normalDetails1 = this.getPayoutDetailDao().findByPayout(dbNormalPayout1.getId());
        assertEquals(1, normalDetails1.size());
        PayoutDetail normalDetail1 = normalDetails1.get(0);
        assertEquals(2200000.0, normalDetail1.getPrizeAmount().doubleValue(), 0);
        assertEquals(860006.0, normalDetail1.getActualAmount().doubleValue(), 0);
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
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

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public NewPrintTicketDao getNewprintTicketDao() {
        return newprintTicketDao;
    }

    public void setNewprintTicketDao(NewPrintTicketDao newprintTicketDao) {
        this.newprintTicketDao = newprintTicketDao;
    }

}
