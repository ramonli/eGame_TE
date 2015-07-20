package com.mpos.lottery.te.gameimpl.union.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.game.UnionGameInstance;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class UnionPrizeIntegrationTest extends BaseServletIntegrationTest {
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

    /**
     * A winning and single-draw ticket.
     */
    @Test
    public void testPayout_PrintNewTicket_Single() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from un_te_ticket t where t.id in ('TICKET-112','TICKET-113')");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027967.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // Commit the transaction. By default spring test framework will
        // rollback the transaction
        // when after a test fixture, but you can change this by setComplete().
        // this.setComplete();

        // check ticket status
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        Payout dbPayout = payouts.get(0);
        assertEquals(dto.getPrizeAmount().doubleValue(), dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(), 0);

        // List<PayoutDetail> details =
        // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        // assertEquals(6, details.size());
        // TODO how to check each PayoutDetail? as we don't know how to find a
        // specific PayoutDetail from the returned list
    }

    @Test
    public void testPayout_PrintNewTicket_Single_ByBarcode() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from un_te_ticket t where t.id in ('TICKET-112','TICKET-113')");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027967.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // Commit the transaction. By default spring test framework will
        // rollback the transaction
        // when after a test fixture, but you can change this by setComplete().
        // this.setComplete();

        // check ticket status
        Barcoder barcoder = new Barcoder(ticket.getBarcode());
        ticket.setRawSerialNo(barcoder.getSerialNo());
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        Payout dbPayout = payouts.get(0);
        assertEquals(dto.getPrizeAmount().doubleValue(), dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(), 0);

        // List<PayoutDetail> details =
        // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        // assertEquals(6, details.size());
        // TODO how to check each PayoutDetail? as we don't know how to find a
        // specific PayoutDetail from the returned list
    }

    /**
     * A winning and single-draw ticket.
     */
    @Rollback(true)
    @Test
    public void testPayout_PrintNewTicket_Single_LuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from un_te_ticket t where t.id in ('TICKET-112','TICKET-113')");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(7832000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1324040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(6507967.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // Commit the transaction. By default spring test framework will
        // rollback the transaction
        // when after a test fixture, but you can change this by setComplete().
        // this.setComplete();

        // check ticket status
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());

        // check payout records
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        Payout dbNormalPayout = this.findPayoutByGameInstance("GII-111", payouts);
        assertEquals(5032000.0, dbNormalPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(4027967.0, dbNormalPayout.getTotalAmount().doubleValue(), 0);
        Payout dbLuckyPayout = this.findPayoutByGameInstance("GII-LD-1", payouts);
        assertEquals(2800000.0, dbLuckyPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(2480000.0, dbLuckyPayout.getTotalAmount().doubleValue(), 0);

        // verify payout details
        List<PayoutDetail> normalDetails = this.getPayoutDetailDao().findByPayout(dbNormalPayout.getId());
        assertEquals(1, normalDetails.size());
        PayoutDetail normalDetail = normalDetails.get(0);
        assertEquals(5032000.0, normalDetail.getPrizeAmount().doubleValue(), 0);
        assertEquals(4027967.0, normalDetail.getActualAmount().doubleValue(), 0);

        List<PayoutDetail> luckyDetails = this.getPayoutDetailDao().findByPayout(dbLuckyPayout.getId());
        assertEquals(2, luckyDetails.size());
        this.sortPayoutDetailByPrizeAmount(luckyDetails);
        PayoutDetail luckyDetail0 = luckyDetails.get(0);
        assertEquals(2000.0, luckyDetail0.getPrizeAmount().doubleValue(), 0);
        assertEquals(1500.0, luckyDetail0.getActualAmount().doubleValue(), 0);
        assertEquals(PrizeLevel.PRIZE_TYPE_OBJECT, luckyDetail0.getPayoutType());

        PayoutDetail luckyDetail1 = luckyDetails.get(1);
        assertEquals(2800000.0, luckyDetail1.getPrizeAmount().doubleValue(), 0);
        assertEquals(2480000.0, luckyDetail1.getActualAmount().doubleValue(), 0);
        assertEquals(PrizeLevel.PRIZE_TYPE_CASH, luckyDetail1.getPayoutType());
    }

    @Test
    public void testPayout_PrintNewTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
        assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
        assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
        assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
        assertEquals(3, dto.getNewPrintTicket().getEntries().size());
        assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
        assertNotNull(dto.getNewPrintTicket().getBarcode());

        UnionTicket respTicket = (UnionTicket) dto.getNewPrintTicket();
        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry1 = respTicket.getEntries().get(1);
        assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
        assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry2 = respTicket.getEntries().get(2);
        assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
        assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-UNION-111");
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
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert new generated ticket
        List<UnionTicket> newPrintTickets = this.getTicketDao().findBySerialNo(UnionTicket.class,
                dto.getNewPrintTicket().getSerialNo(), false);
        assertEquals(1, newPrintTickets.size());
        UnionTicket expectedTicket = new UnionTicket();
        expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
        expectedTicket.setDevId(111);
        expectedTicket.setMerchantId(111);
        expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
        expectedTicket.setMultipleDraws(1);
        expectedTicket.setOperatorId(reqCtx.getOperatorId());
        expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
        expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        // simply make it equal with DB
        expectedTicket.setTotalBets(1);
        expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
        expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
        expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectedTicket.setGameInstance(gameInstance);
        this.assertTicket(expectedTicket, newPrintTickets.get(0));

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        // TODO we should check each column of each record!!

        // List<PayoutDetail> details =
        // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        // assertEquals(6, details.size());
        // TODO how to check each PayoutDetail? as we don't know how to find a
        // specific PayoutDetail from the returned list
    }

    @Test
    public void testPayout_PrintNewTicket_TaxWhenPayoutBased_PerTicket_NoLuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1205333.34, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(6026666.66, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
        assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
        assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
        assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
        assertEquals(3, dto.getNewPrintTicket().getEntries().size());
        assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
        assertNotNull(dto.getNewPrintTicket().getBarcode());

        UnionTicket respTicket = (UnionTicket) dto.getNewPrintTicket();
        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry1 = respTicket.getEntries().get(1);
        assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
        assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry2 = respTicket.getEntries().get(2);
        assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
        assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-UNION-111");
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
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert new generated ticket
        List<UnionTicket> newPrintTickets = this.getTicketDao().findBySerialNo(UnionTicket.class,
                dto.getNewPrintTicket().getSerialNo(), false);
        assertEquals(1, newPrintTickets.size());
        UnionTicket expectedTicket = new UnionTicket();
        expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
        expectedTicket.setDevId(111);
        expectedTicket.setMerchantId(111);
        expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
        expectedTicket.setMultipleDraws(1);
        expectedTicket.setOperatorId(reqCtx.getOperatorId());
        expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
        expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        // simply make it equal with DB
        expectedTicket.setTotalBets(1);
        expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
        expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectedTicket.setGameInstance(gameInstance);
        expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
        this.assertTicket(expectedTicket, newPrintTickets.get(0));

        // check payout records
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        this.sortPayoutByPrizeAmount(payouts);

        assertEquals(1833333.33, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2200000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("GAME-UNION-111", payouts.get(0).getGameId());
        assertEquals("GII-112", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());

        assertEquals(4193333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
        assertEquals(5032000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("GAME-UNION-111", payouts.get(1).getGameId());
        assertEquals("GII-111", payouts.get(1).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());
    }

    @Test
    public void testPayout_PrintNewTicket_TaxWhenPayoutBased_PerTicket_WinLuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1672000.01, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(8359999.99, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
        assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
        assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
        assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
        assertEquals(3, dto.getNewPrintTicket().getEntries().size());
        assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
        assertNotNull(dto.getNewPrintTicket().getBarcode());

        UnionTicket respTicket = (UnionTicket) dto.getNewPrintTicket();
        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry1 = respTicket.getEntries().get(1);
        assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
        assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
        BaseEntry respEntry2 = respTicket.getEntries().get(2);
        assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
        assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
        assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-UNION-111");
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
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert new generated ticket
        List<UnionTicket> newPrintTickets = this.getTicketDao().findBySerialNo(UnionTicket.class,
                dto.getNewPrintTicket().getSerialNo(), false);
        assertEquals(1, newPrintTickets.size());
        UnionTicket expectedTicket = new UnionTicket();
        expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
        expectedTicket.setDevId(111);
        expectedTicket.setMerchantId(111);
        expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
        expectedTicket.setMultipleDraws(1);
        expectedTicket.setOperatorId(reqCtx.getOperatorId());
        expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
        expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        // simply make it equal with DB
        expectedTicket.setTotalBets(1);
        expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
        expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectedTicket.setGameInstance(gameInstance);
        expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
        this.assertTicket(expectedTicket, newPrintTickets.get(0));

        // check payout records
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, payouts.size());
        this.sortPayoutByPrizeAmount(payouts);

        assertEquals(1833333.33, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2200000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("GAME-UNION-111", payouts.get(0).getGameId());
        assertEquals("GII-112", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());

        assertEquals(2333333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LD-1", payouts.get(1).getGameId());
        assertEquals("GII-LD-1", payouts.get(1).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());

        assertEquals(4193333.33, payouts.get(2).getTotalAmount().doubleValue(), 0);
        assertEquals(5032000.0, payouts.get(2).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("GAME-UNION-111", payouts.get(2).getGameId());
        assertEquals("GII-111", payouts.get(2).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(2).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(2).getStatus());

        // TODO check payout details as well
    }

    @Test
    public void testPayout_PrintNewTicket_NoWin() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
        assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
        assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
        assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
        assertEquals(3, dto.getNewPrintTicket().getEntries().size());

        // check ticket status
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert new generated ticket
        List<UnionTicket> newPrintTickets = this.getTicketDao().findBySerialNo(UnionTicket.class,
                dto.getNewPrintTicket().getSerialNo(), false);
        assertEquals(1, newPrintTickets.size());
        UnionTicket expectedTicket = new UnionTicket();
        expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
        expectedTicket.setDevId(111);
        expectedTicket.setMerchantId(111);
        expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
        expectedTicket.setMultipleDraws(1);
        expectedTicket.setOperatorId(reqCtx.getOperatorId());
        expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
        expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectedTicket.setTotalBets(1);
        expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
        expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectedTicket.setGameInstance(gameInstance);
        expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
        this.assertTicket(expectedTicket, newPrintTickets.get(0));

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(0, payouts.size());
    }

    @Test
    public void testPayout_Return() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4890473.1, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-UNION-111");
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
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, payouts.size());
        // TODO we should check each column of each record!!

        // List<PayoutDetail> details =
        // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        // assertEquals(6, details.size());
        // TODO how to check each PayoutDetail? as we don't know how to find a
        // specific PayoutDetail from the returned list
    }

    /**
     * No win, however should return amount of advanced draw.
     */
    @Test
    public void testPayout_Return_NoWin() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        // check ticket status
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
    }

    /**
     * No win, however should return amount of advanced draw.
     */
    @Rollback(true)
    @Test
    public void testPayout_Return_NoNormalWin_WinLucky() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2482500.1, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        // check ticket status
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
    }

    @Test
    public void testPayout_Return_TaxWhenPayout_BasedPerTicket_WinOnlyLuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(466666.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2335833.43, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("GAME-UNION-111");
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
        // List<UnionTicket> dbTickets =
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(0).getStatus());
        assertEquals(UnionTicket.STATUS_PAID, dbTickets.get(1).getStatus());
        // confirm payout will set this ticket to invalid
        assertEquals(UnionTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());

        // check payout records
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        this.sortPayoutByPrizeAmount(payouts);

        assertEquals(2500.1, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2500.1, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("GAME-UNION-111", payouts.get(0).getGameId());
        assertEquals("GII-113", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_RETURN, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());

        assertEquals(2333333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LD-1", payouts.get(1).getGameId());
        assertEquals("GII-LD-1", payouts.get(1).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());
    }

    @Test
    public void testPayout_ExceedPayoutLimit() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update BD_PRIZE_GROUP set max_value=1000 where id='BPG-1'");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_MAX_PAYOUT, respCtx.getResponseCode());
    }

    @Test
    public void testPayout_underPayoutLimit() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update BD_PRIZE_GROUP set min_value=200000000,max_value=300000000 where id='BPG-1'");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_MAX_PAYOUT, respCtx.getResponseCode());
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update UN_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
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

    public void setEntryDao(BaseEntryDao entryDao) {
        this.entryDao = entryDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

}
