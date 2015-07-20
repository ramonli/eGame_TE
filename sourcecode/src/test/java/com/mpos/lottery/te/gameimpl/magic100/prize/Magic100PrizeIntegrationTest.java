package com.mpos.lottery.te.gameimpl.magic100.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

/**
 * 
 * @author terry
 * @version 2014-6-17
 */
public class Magic100PrizeIntegrationTest extends BaseServletIntegrationTest {
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

    @Test
    public void testPayoutOK() throws Exception {
        printMethod();
        Magic100Ticket ticket = new Magic100Ticket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(1);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");
        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1000, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(833.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("LK-1");
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
        // List<LottoTicket> dbTickets =
        List<Magic100Ticket> dbTickets = this.getTicketDao().findBySerialNo(Magic100Ticket.class, ticket.getSerialNo(),
                false);
        assertEquals(Magic100Ticket.STATUS_PAID, dbTickets.get(0).getStatus());
        // assertEquals(Magic100Ticket.STATUS_PAID, dbTickets.get(1).getStatus());
        // // confirm payout will set this ticket to invalid
        // assertEquals(Magic100Ticket.STATUS_RETURNED, dbTickets.get(2).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
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

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

}
