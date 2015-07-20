package com.mpos.lottery.te.gameimpl.bingo.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class BingoPrizeIntegrationTest extends BaseServletIntegrationTest {
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

    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    /**
     * A winning and single-draw ticket.
     */
    @Test
    public void testPayout_PrintNewTicket_Single_operator() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from TE_BG_TICKET t where t.id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate
                .update("update merchant_game_properties mg set mg.commission_rate_payout=0.1 where MRID like '%-BINGO-%'");

        Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027982.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {

            // commission
            Operator operator = operatorDao.findById(Operator.class, "OPERATOR-111");
            assertEquals(402798.2, operator.getCommisionBalance().doubleValue(), 0);

            // commission balance
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            assertEquals(1, list.size());
            assertEquals(BalanceTransactions.STATUS_VALID, balanceTransactions.getStatus());
            assertEquals(4027982, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
            assertEquals(402798.2, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
            assertEquals(0.1, balanceTransactions.getCommissionRate().doubleValue(), 0.0);
        }
        // Commit the transaction. By default spring test framework will
        // rollback the transaction
        // when after a test fixture, but you can change this by setComplete().
        // this.setComplete();

        // check ticket status
        List<BingoTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
        assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());

        // check payout records
        // List<Payout> payouts =
        this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        Payout dbPayout = payouts.get(0);
        // assertEquals(5032000.0, dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        // assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(),
        // 0);

        // List<PayoutDetail> details =
        // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        // assertEquals(6, details.size());
        // TODO how to check each PayoutDetail? as we don't know how to find a
        // specific PayoutDetail from the returned list
    }

    // @Test
    // public void testPayout_PrintNewTicket_Single_Operator_Cancelled() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from TE_BG_TICKET t where t.id in ('2','3')");
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate
    // .update("update merchant_game_properties mg set mg.commission_rate_payout=0.1  where MRID like '%-BINGO-%'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0, dto.getGeneratedTickets().size());
    // assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4027982.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    //
    // // Commit the transaction. By default spring test framework will
    // // rollback the transaction
    // // when after a test fixture, but you can change this by setComplete().
    // // this.setComplete();
    //
    // // check ticket status
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(1, payouts.size());
    // Payout dbPayout = payouts.get(0);
    // // assertEquals(5032000.0, dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(),
    // // 0);
    //
    // // List<PayoutDetail> details =
    // // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
    // // assertEquals(6, details.size());
    // // TODO how to check each PayoutDetail? as we don't know how to find a
    // // specific PayoutDetail from the returned list
    //
    // // 2. make cancellation
    // Transaction trans = new Transaction();
    // trans.setDeviceId(reqCtx.getTerminalId());
    // trans.setTraceMessageId(reqCtx.getTraceMessageId());
    // Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
    // Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
    //
    // // force to flush to underlying database to avoid FALSE POSITIVE
    // this.entityManager.flush();
    // this.entityManager.clear();
    // if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
    //
    // // commission
    // Operator operator = operatorDao.findById(Operator.class, "OPERATOR-111");
    // // assertEquals(402798.2, operator.getCommisionBalance().doubleValue(), 0);
    //
    // operator = operatorDao.findById(Operator.class, "OPERATOR-111");
    // assertEquals(0.0, operator.getCommisionBalance().doubleValue(), 0);
    //
    // List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
    // .getTransactionID());
    // List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
    // BalanceTransactions balanceTransactions = list.get(0);
    // BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);
    //
    // assertEquals(1, cancellationList.size());
    // assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
    // assertEquals(4027982.0, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
    // assertEquals("OPERATOR-111", cancellationBalanceTransactions.getOwnerId());
    // assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancellationBalanceTransactions.getOwnerType());
    // assertEquals(-402798.2, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);
    //
    // assertEquals(1, list.size());
    // assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
    // }
    //
    // }
    //
    // /**
    // * A winning and single-draw ticket.
    // */
    // @Test
    // public void testPayout_PrintNewTicket_Single_merchant() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from TE_BG_TICKET t where t.id in ('2','3')");
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate
    // .update("update merchant_game_properties mg set mg.commission_rate_payout=0.1  where MRID like '%-BINGO-%'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0, dto.getGeneratedTickets().size());
    // assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4027982.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
    //
    // // commission
    // Operator operator = operatorDao.findById(Operator.class, "OPERATOR-111");
    // assertEquals(402798.2, operator.getCommisionBalance().doubleValue(), 0);
    //
    // // commission balance
    // List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
    // BalanceTransactions balanceTransactions = list.get(0);
    // assertEquals(1, list.size());
    // assertEquals(BalanceTransactions.STATUS_VALID, balanceTransactions.getStatus());
    // assertEquals(4027982, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
    // assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
    // assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
    // assertEquals(402798.2, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
    // assertEquals(0.1, balanceTransactions.getCommissionRate().doubleValue(), 0.0);
    // }
    // // Commit the transaction. By default spring test framework will
    // // rollback the transaction
    // // when after a test fixture, but you can change this by setComplete().
    // // this.setComplete();
    //
    // // check ticket status
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(1, payouts.size());
    // Payout dbPayout = payouts.get(0);
    // // assertEquals(5032000.0, dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(),
    // // 0);
    //
    // // List<PayoutDetail> details =
    // // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
    // // assertEquals(6, details.size());
    // // TODO how to check each PayoutDetail? as we don't know how to find a
    // // specific PayoutDetail from the returned list
    // }
    //
    // @Test
    // public void testPayout_PrintNewTicket_Single_ByBarcode() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from te_ticket t where t.id in ('2','3')");
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0, dto.getGeneratedTickets().size());
    // assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4027982.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // Commit the transaction. By default spring test framework will
    // // rollback the transaction
    // // when after a test fixture, but you can change this by setComplete().
    // // this.setComplete();
    //
    // // check ticket status
    // Barcoder barcoder = new Barcoder(ticket.getBarcode());
    // ticket.setRawSerialNo(barcoder.getSerialNo());
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(1, payouts.size());
    // Payout dbPayout = payouts.get(0);
    // // assertEquals(dto.getPrizeAmount().doubleValue(),
    // // dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals(dto.getActualAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(),
    // // 0);
    //
    // // List<PayoutDetail> details =
    // // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
    // // assertEquals(6, details.size());
    // // TODO how to check each PayoutDetail? as we don't know how to find a
    // // specific PayoutDetail from the returned list
    // }
    //
    // // @Rollback(true)
    // // @Test
    // // public void testPayout_PrintNewTicket() throws Exception {
    // // printMethod();
    // // BingoTicket ticket = new BingoTicket();
    // // ticket.setRawSerialNo("S-123456"); // print new ticket
    // // ticket.setValidationCode("111111");
    // // ticket.setPIN("PIN-111");
    // // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // //
    // // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // // this.jdbcTemplate.update("delete from ld_winning");
    // //
    // // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // // PrizeDto prize = (PrizeDto) respCtx.getModel();
    // //
    // // this.entityManager.flush();
    // // this.entityManager.clear();
    // //
    // // // assert response
    // // assertEquals(200, respCtx.getResponseCode());
    // // assertNotNull(respCtx.getModel());
    // // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // // assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
    // // assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
    // // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // // asert new print ticket
    // // assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
    // // assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
    // // assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
    // // assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
    // // assertEquals(3, dto.getNewPrintTicket().getEntries().size());
    // // assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
    // // assertNotNull(dto.getNewPrintTicket().getBarcode());
    // //
    // // BingoTicket respTicket = (BingoTicket) dto.getNewPrintTicket();
    // // this.sortTicketEntries(respTicket.getEntries());
    // // BaseEntry respEntry0 = respTicket.getEntries().get(0);
    // // assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
    // // assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
    // // BaseEntry respEntry1 = respTicket.getEntries().get(1);
    // // assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
    // // assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
    // // // assertEquals(50.0, ((BingoEntry) respEntry1).getBoostAmount().doubleValue(), 0);
    // // BaseEntry respEntry2 = respTicket.getEntries().get(2);
    // // assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
    // // assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);
    // //
    // // // assert Transaction
    // // Transaction expectedTrans = new Transaction();
    // // expectedTrans.setId(respCtx.getTransactionID());
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setGameId("GAME-111");
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setTicketSerialNo(ticket.getSerialNo());
    // // expectedTrans.setOperatorId(respCtx.getOperatorId());
    // // expectedTrans.setMerchantId(111);
    // // expectedTrans.setDeviceId(respCtx.getTerminalId());
    // // expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
    // // expectedTrans.setType(reqCtx.getTransType());
    // // expectedTrans.setResponseCode(SystemException.CODE_OK);
    // // Transaction dbTrans = this.getTransactionDao()
    // // .findById(Transaction.class, respCtx.getTransactionID());
    // // this.assertTransaction(expectedTrans, dbTrans);
    // //
    // // // check ticket status
    // // // List<BingoTicket> dbTickets =
    // // List<BingoTicket> dbTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // ticket.getSerialNo(), false);
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(1).getStatus());
    // // // confirm payout will set this ticket to invalid
    // // assertEquals(BingoTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());
    // //
    // // // assert new generated ticket
    // // List<BingoTicket> newPrintTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // dto.getNewPrintTicket().getSerialNo(), false);
    // // assertEquals(1, newPrintTickets.size());
    // // BingoTicket expectedTicket = new BingoTicket();
    // // expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
    // // expectedTicket.setDevId(111);
    // // expectedTicket.setMerchantId(111);
    // // expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
    // // expectedTicket.setMultipleDraws(1);
    // // expectedTicket.setOperatorId(reqCtx.getOperatorId());
    // // expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
    // // expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
    // // expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
    // // expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
    // // // simply make it equal with DB
    // // expectedTicket.setTotalBets(1);
    // // expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
    // // expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
    // // BingoGameInstance gameInstance = new BingoGameInstance();
    // // gameInstance.setId("GII-113");
    // // expectedTicket.setGameInstance(gameInstance);
    // // // expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
    // // this.assertTicket(expectedTicket, newPrintTickets.get(0));
    // //
    // // // assert generated entries
    // // List<BingoEntry> newEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
    // // newPrintTickets.get(0).getSerialNo(), false);
    // // assertEquals(3, newEntries.size());
    // // this.sortTicketEntries(newEntries);
    // // assertEquals("1,2,3,4,5", newEntries.get(0).getSelectNumber());
    // // assertEquals("1,2,3,4,5,6", newEntries.get(1).getSelectNumber());
    // // assertEquals("1,2,3,4,5,6,7", newEntries.get(2).getSelectNumber());
    // //
    // // // check payout records
    // // // List<Payout> payouts =
    // // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // assertEquals(2, payouts.size());
    // // // TODO we should check each column of each record!!
    // //
    // // // List<PayoutDetail> details =
    // // // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
    // // // assertEquals(6, details.size());
    // // // TODO how to check each PayoutDetail? as we don't know how to find a
    // // // specific PayoutDetail from the returned list
    // // }
    //
    // // @Test
    // // public void testPayout_PrintNewTicket_TaxWhenPayoutBased_PerTicket_NoLuckyDraw() throws
    // // Exception {
    // // printMethod();
    // // BingoTicket ticket = new BingoTicket();
    // // ticket.setRawSerialNo("S-123456"); // print new ticket
    // // ticket.setValidationCode("111111");
    // // ticket.setPIN("PIN-111");
    // // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // //
    // // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    // // this.jdbcTemplate.update("delete from ld_winning");
    // //
    // // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // // PrizeDto prize = (PrizeDto) respCtx.getModel();
    // //
    // // this.entityManager.flush();
    // // this.entityManager.clear();
    // //
    // // // assert response
    // // assertEquals(200, respCtx.getResponseCode());
    // // assertNotNull(respCtx.getModel());
    // // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // // assertEquals(1205333.34, dto.getTaxAmount().doubleValue(), 0);
    // // assertEquals(6026666.66, dto.getActualAmount().doubleValue(), 0);
    // // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // // asert new print ticket
    // // assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
    // // assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
    // // assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
    // // assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
    // // assertEquals(3, dto.getNewPrintTicket().getEntries().size());
    // // assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
    // // assertNotNull(dto.getNewPrintTicket().getBarcode());
    // //
    // // BingoTicket respTicket = (BingoTicket) dto.getNewPrintTicket();
    // // this.sortTicketEntries(respTicket.getEntries());
    // // BaseEntry respEntry0 = respTicket.getEntries().get(0);
    // // assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
    // // assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
    // // BaseEntry respEntry1 = respTicket.getEntries().get(1);
    // // assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
    // // assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
    // // // assertEquals(50.0, ((BingoEntry) respEntry1).getBoostAmount().doubleValue(), 0);
    // // BaseEntry respEntry2 = respTicket.getEntries().get(2);
    // // assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
    // // assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);
    // //
    // // // assert Transaction
    // // Transaction expectedTrans = new Transaction();
    // // expectedTrans.setId(respCtx.getTransactionID());
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setGameId("GAME-111");
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setTicketSerialNo(ticket.getSerialNo());
    // // expectedTrans.setOperatorId(respCtx.getOperatorId());
    // // expectedTrans.setMerchantId(111);
    // // expectedTrans.setDeviceId(respCtx.getTerminalId());
    // // expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
    // // expectedTrans.setType(reqCtx.getTransType());
    // // expectedTrans.setResponseCode(SystemException.CODE_OK);
    // // Transaction dbTrans = this.getTransactionDao()
    // // .findById(Transaction.class, respCtx.getTransactionID());
    // // this.assertTransaction(expectedTrans, dbTrans);
    // //
    // // // check ticket status
    // // // List<BingoTicket> dbTickets =
    // // List<BingoTicket> dbTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // ticket.getSerialNo(), false);
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(1).getStatus());
    // // // confirm payout will set this ticket to invalid
    // // assertEquals(BingoTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());
    // //
    // // // assert new generated ticket
    // // List<BingoTicket> newPrintTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // dto.getNewPrintTicket().getSerialNo(), false);
    // // assertEquals(1, newPrintTickets.size());
    // // BingoTicket expectedTicket = new BingoTicket();
    // // expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
    // // expectedTicket.setDevId(111);
    // // expectedTicket.setMerchantId(111);
    // // expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
    // // expectedTicket.setMultipleDraws(1);
    // // expectedTicket.setOperatorId(reqCtx.getOperatorId());
    // // expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
    // // expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
    // // expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
    // // expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
    // // // simply make it equal with DB
    // // expectedTicket.setTotalBets(1);
    // // expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
    // // expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
    // // BingoGameInstance gameInstance = new BingoGameInstance();
    // // gameInstance.setId("GII-113");
    // // expectedTicket.setGameInstance(gameInstance);
    // // // expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
    // // this.assertTicket(expectedTicket, newPrintTickets.get(0));
    // //
    // // // check payout records
    // // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // assertEquals(2, payouts.size());
    // // this.sortPayoutByPrizeAmount(payouts);
    // //
    // // assertEquals(1833333.33, payouts.get(0).getTotalAmount().doubleValue(), 0);
    // // assertEquals(2200000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("GAME-111", payouts.get(0).getGameId());
    // // assertEquals("GII-112", payouts.get(0).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
    // //
    // // assertEquals(4193333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
    // // assertEquals(5032000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("GAME-111", payouts.get(1).getGameId());
    // // assertEquals("GII-111", payouts.get(1).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());
    // // }
    // //
    // // @Test
    // // public void testPayout_PrintNewTicket_TaxWhenPayoutBased_PerTicket_WinSecondPrize() throws
    // // Exception {
    // // printMethod();
    // // BingoTicket ticket = new BingoTicket();
    // // ticket.setRawSerialNo("S-123456"); // print new ticket
    // // ticket.setValidationCode("111111");
    // // ticket.setPIN("PIN-111");
    // // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // //
    // // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    // //
    // // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // // PrizeDto prize = (PrizeDto) respCtx.getModel();
    // //
    // // this.entityManager.flush();
    // // this.entityManager.clear();
    // //
    // // // assert response
    // // assertEquals(200, respCtx.getResponseCode());
    // // assertNotNull(respCtx.getModel());
    // // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // // assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
    // // assertEquals(1672000.01, dto.getTaxAmount().doubleValue(), 0);
    // // assertEquals(8359999.99, dto.getActualAmount().doubleValue(), 0);
    // // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // // asert new print ticket
    // // assertEquals(2500.1, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
    // // assertEquals("20090408", dto.getNewPrintTicket().getLastDrawNo());
    // // assertEquals(1, dto.getNewPrintTicket().getMultipleDraws());
    // // assertEquals("20090408", dto.getNewPrintTicket().getGameInstance().getNumber());
    // // assertEquals(3, dto.getNewPrintTicket().getEntries().size());
    // // assertEquals(6, dto.getNewPrintTicket().getValidationCode().length());
    // // assertNotNull(dto.getNewPrintTicket().getBarcode());
    // //
    // // BingoTicket respTicket = (BingoTicket) dto.getNewPrintTicket();
    // // this.sortTicketEntries(respTicket.getEntries());
    // // BaseEntry respEntry0 = respTicket.getEntries().get(0);
    // // assertEquals("1,2,3,4,5", respEntry0.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_BANKER, respEntry0.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry0.getInputChannel());
    // // assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
    // // BaseEntry respEntry1 = respTicket.getEntries().get(1);
    // // assertEquals("1,2,3,4,5,6", respEntry1.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_SINGLE, respEntry1.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry1.getInputChannel());
    // // assertEquals(100.0, respEntry1.getEntryAmount().doubleValue(), 0);
    // // // assertEquals(50.0, ((BingoEntry) respEntry1).getBoostAmount().doubleValue(), 0);
    // // BaseEntry respEntry2 = respTicket.getEntries().get(2);
    // // assertEquals("1,2,3,4,5,6,7", respEntry2.getSelectNumber());
    // // assertEquals(BaseEntry.BETOPTION_MULTIPLE, respEntry2.getBetOption());
    // // assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, respEntry2.getInputChannel());
    // // assertEquals(100.0, respEntry2.getEntryAmount().doubleValue(), 0);
    // //
    // // // assert Transaction
    // // Transaction expectedTrans = new Transaction();
    // // expectedTrans.setId(respCtx.getTransactionID());
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setGameId("GAME-111");
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setTicketSerialNo(ticket.getSerialNo());
    // // expectedTrans.setOperatorId(respCtx.getOperatorId());
    // // expectedTrans.setMerchantId(111);
    // // expectedTrans.setDeviceId(respCtx.getTerminalId());
    // // expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
    // // expectedTrans.setType(reqCtx.getTransType());
    // // expectedTrans.setResponseCode(SystemException.CODE_OK);
    // // Transaction dbTrans = this.getTransactionDao()
    // // .findById(Transaction.class, respCtx.getTransactionID());
    // // this.assertTransaction(expectedTrans, dbTrans);
    // //
    // // // check ticket status
    // // // List<BingoTicket> dbTickets =
    // // List<BingoTicket> dbTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // ticket.getSerialNo(), false);
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(1).getStatus());
    // // // confirm payout will set this ticket to invalid
    // // assertEquals(BingoTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());
    // //
    // // // assert new generated ticket
    // // List<BingoTicket> newPrintTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // dto.getNewPrintTicket().getSerialNo(), false);
    // // assertEquals(1, newPrintTickets.size());
    // // BingoTicket expectedTicket = new BingoTicket();
    // // expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
    // // expectedTicket.setDevId(111);
    // // expectedTicket.setMerchantId(111);
    // // expectedTicket.setTotalAmount(new BigDecimal("2500.1"));
    // // expectedTicket.setMultipleDraws(1);
    // // expectedTicket.setOperatorId(reqCtx.getOperatorId());
    // // expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
    // // expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
    // // expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
    // // expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
    // // // simply make it equal with DB
    // // expectedTicket.setTotalBets(1);
    // // expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
    // // expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
    // // BingoGameInstance gameInstance = new BingoGameInstance();
    // // gameInstance.setId("GII-113");
    // // // expectedTicket.setExtendText(dbTickets.get(0).getExtendText());
    // // expectedTicket.setGameInstance(gameInstance);
    // // this.assertTicket(expectedTicket, newPrintTickets.get(0));
    // //
    // // // check payout records
    // // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // assertEquals(3, payouts.size());
    // // this.sortPayoutByPrizeAmount(payouts);
    // //
    // // assertEquals(1833333.33, payouts.get(0).getTotalAmount().doubleValue(), 0);
    // // assertEquals(2200000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("GAME-111", payouts.get(0).getGameId());
    // // assertEquals("GII-112", payouts.get(0).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
    // //
    // // assertEquals(2333333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
    // // assertEquals(2800000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("LD-1", payouts.get(1).getGameId());
    // // assertEquals("GII-LD-1", payouts.get(1).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());
    // //
    // // assertEquals(4193333.33, payouts.get(2).getTotalAmount().doubleValue(), 0);
    // // assertEquals(5032000.0, payouts.get(2).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("GAME-111", payouts.get(2).getGameId());
    // // assertEquals("GII-111", payouts.get(2).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(2).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(2).getStatus());
    // //
    // // // TODO check payout details as well
    // // }
    //
    // @Test
    // public void testPayout_PrintNewTicket_NoWin() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from bg_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate.update("delete from bg_winning_lucky");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // asert new print ticket
    // assertEquals(1400, dto.getNewPrintTicket().getTotalAmount().doubleValue(), 0);
    // assertEquals("11003", dto.getNewPrintTicket().getLastDrawNo());
    // assertEquals(2, dto.getNewPrintTicket().getMultipleDraws());
    // assertEquals("11002", dto.getNewPrintTicket().getGameInstance().getNumber());
    // assertEquals(2, dto.getNewPrintTicket().getEntries().size());
    //
    // // assert new generated ticket
    // List<BingoTicket> newPrintTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // dto.getNewPrintTicket().getSerialNo(), false);
    // assertEquals(2, newPrintTickets.size());
    // BingoTicket expectedTicket = new BingoTicket();
    // expectedTicket.setSerialNo(newPrintTickets.get(0).getSerialNo());
    // expectedTicket.setDevId(111);
    // expectedTicket.setMerchantId(111);
    // expectedTicket.setTotalAmount(new BigDecimal("700"));
    // expectedTicket.setMultipleDraws(2);
    // expectedTicket.setOperatorId(reqCtx.getOperatorId());
    // expectedTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
    // expectedTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
    // expectedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
    // expectedTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
    // expectedTicket.setTotalBets(1);
    // expectedTicket.setValidationCode(dto.getNewPrintTicket().getValidationCode());
    // expectedTicket.setBarcode(dto.getNewPrintTicket().getBarcode());
    // BingoGameInstance gameInstance = new BingoGameInstance();
    // gameInstance.setId("GII-112");
    // expectedTicket.setGameInstance(gameInstance);
    // this.assertTicket(expectedTicket, newPrintTickets.get(0));
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(0, payouts.size());
    // }
    //
    // @Test
    // public void testPayout_Return() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // PrizeDto prize = (PrizeDto) respCtx.getModel();
    //
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4029382, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(1400, dto.getReturnAmount().doubleValue(), 0);
    // // asert new print ticket
    // assertEquals(true, dto.getNewPrintTicket() == null);
    //
    // // assert Transaction
    // Transaction expectedTrans = new Transaction();
    // expectedTrans.setId(respCtx.getTransactionID());
    // expectedTrans.setTotalAmount(prize.getActualAmount());
    // expectedTrans.setGameId("BINGO-1");
    // expectedTrans.setTotalAmount(prize.getActualAmount());
    // expectedTrans.setTicketSerialNo(ticket.getSerialNo());
    // expectedTrans.setOperatorId(respCtx.getOperatorId());
    // expectedTrans.setMerchantId(111);
    // expectedTrans.setDeviceId(respCtx.getTerminalId());
    // expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
    // expectedTrans.setType(reqCtx.getTransType());
    // expectedTrans.setResponseCode(SystemException.CODE_OK);
    // Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
    // this.assertTransaction(expectedTrans, dbTrans);
    //
    // // check ticket status
    // // List<BingoTicket> dbTickets =
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(1).getStatus());
    // // confirm payout will set this ticket to invalid
    // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(3, payouts.size());
    // // TODO we should check each column of each record!!
    //
    // // List<PayoutDetail> details =
    // // this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
    // // assertEquals(6, details.size());
    // // TODO how to check each PayoutDetail? as we don't know how to find a
    // // specific PayoutDetail from the returned list
    // }
    //
    // /**
    // * No win, however should return amount of advanced draw.
    // */
    // @Test
    // public void testPayout_Return_NoNormalWin_winSecondPrize() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from bg_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(50.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(35.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(1415.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(1400, dto.getReturnAmount().doubleValue(), 0);
    // // asert new print ticket
    // assertEquals(true, dto.getNewPrintTicket() == null);
    //
    // // check ticket status
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(1).getStatus());
    // // confirm payout will set this ticket to invalid
    // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(3, payouts.size());
    // }
    //
    // /**
    // * No win, however should return amount of advanced draw.
    // */
    // @Test
    // public void testPayout_Return_NoNormalWin_winSecondPrize_only_objectPrize() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from bg_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
    // this.jdbcTemplate.update("delete from TE_BG_TICKET where BG_GAME_INSTANCE_ID='GII-112'");
    // this.jdbcTemplate.update("delete from TE_BG_TICKET where BG_GAME_INSTANCE_ID='GII-113'");
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate.update("delete from BG_LUCKY_PRIZE_RESULT where PRIZE_LEVEL in(3,4)");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // // asert new print ticket
    // assertEquals(true, dto.getNewPrintTicket() == null);
    //
    // // check ticket status
    // List<BingoTicket> dbTickets = this.getTicketDao()
    // .findBySerialNo(BingoTicket.class, ticket.getSerialNo(), false);
    // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    //
    // // check payout records
    // // List<Payout> payouts =
    // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // assertEquals(1, payouts.size());
    // }
    //
    // // /**
    // // * No win, however should return amount of advanced draw.
    // // */
    // // @Rollback(true)
    // // @Test
    // // public void testPayout_Return_NoNormalWin_WinLucky() throws Exception {
    // // printMethod();
    // // BingoTicket ticket = new BingoTicket();
    // // ticket.setRawSerialNo("S-123456"); // print new ticket
    // // ticket.setValidationCode("111111");
    // // ticket.setPIN("PIN-111");
    // // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // //
    // // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // // ticket.getSerialNo()
    // // + "'");
    // //
    // // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // //
    // // assertEquals(200, respCtx.getResponseCode());
    // // assertNotNull(respCtx.getModel());
    // // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // // assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
    // // assertEquals(320000.0, dto.getTaxAmount().doubleValue(), 0);
    // // assertEquals(2482500.1, dto.getActualAmount().doubleValue(), 0);
    // // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // // assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    // // // asert new print ticket
    // // assertEquals(true, dto.getNewPrintTicket() == null);
    // //
    // // // check ticket status
    // // List<BingoTicket> dbTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // ticket.getSerialNo(), false);
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(1).getStatus());
    // // // confirm payout will set this ticket to invalid
    // // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());
    // //
    // // // check payout records
    // // // List<Payout> payouts =
    // // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // assertEquals(2, payouts.size());
    // // }
    // //
    // // @Test
    // // public void testPayout_Return_TaxWhenPayout_BasedPerTicket_WinOnlyLuckyDraw() throws Exception
    // // {
    // // printMethod();
    // // BingoTicket ticket = new BingoTicket();
    // // ticket.setRawSerialNo("S-123456"); // print new ticket
    // // ticket.setValidationCode("111111");
    // // ticket.setPIN("PIN-111");
    // // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    // //
    // // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    // // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // // ticket.getSerialNo()
    // // + "'");
    // //
    // // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // // PrizeDto prize = (PrizeDto) respCtx.getModel();
    // //
    // // this.entityManager.flush();
    // // this.entityManager.clear();
    // //
    // // // assert response
    // // assertEquals(200, respCtx.getResponseCode());
    // // assertNotNull(respCtx.getModel());
    // // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // // assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
    // // assertEquals(466666.67, dto.getTaxAmount().doubleValue(), 0);
    // // assertEquals(2335833.43, dto.getActualAmount().doubleValue(), 0);
    // // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // // assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    // // // asert new print ticket
    // // assertEquals(true, dto.getNewPrintTicket() == null);
    // //
    // // // assert Transaction
    // // Transaction expectedTrans = new Transaction();
    // // expectedTrans.setId(respCtx.getTransactionID());
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setGameId("GAME-111");
    // // expectedTrans.setTotalAmount(prize.getActualAmount());
    // // expectedTrans.setTicketSerialNo(ticket.getSerialNo());
    // // expectedTrans.setOperatorId(respCtx.getOperatorId());
    // // expectedTrans.setMerchantId(111);
    // // expectedTrans.setDeviceId(respCtx.getTerminalId());
    // // expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
    // // expectedTrans.setType(reqCtx.getTransType());
    // // expectedTrans.setResponseCode(SystemException.CODE_OK);
    // // Transaction dbTrans = this.getTransactionDao()
    // // .findById(Transaction.class, respCtx.getTransactionID());
    // // this.assertTransaction(expectedTrans, dbTrans);
    // //
    // // // check ticket status
    // // // List<BingoTicket> dbTickets =
    // // List<BingoTicket> dbTickets = this.getTicketDao().findBySerialNo(BingoTicket.class,
    // // ticket.getSerialNo(), false);
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(0).getStatus());
    // // assertEquals(BingoTicket.STATUS_PAID, dbTickets.get(1).getStatus());
    // // // confirm payout will set this ticket to invalid
    // // assertEquals(BingoTicket.STATUS_RETURNED, dbTickets.get(2).getStatus());
    // //
    // // // check payout records
    // // this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
    // // assertEquals(2, payouts.size());
    // // this.sortPayoutByPrizeAmount(payouts);
    // //
    // // assertEquals(2500.1, payouts.get(0).getTotalAmount().doubleValue(), 0);
    // // assertEquals(2500.1, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("GAME-111", payouts.get(0).getGameId());
    // // assertEquals("GII-113", payouts.get(0).getGameInstanceId());
    // // assertEquals(Payout.TYPE_RETURN, payouts.get(0).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
    // //
    // // assertEquals(2333333.33, payouts.get(1).getTotalAmount().doubleValue(), 0);
    // // assertEquals(2800000.0, payouts.get(1).getBeforeTaxTotalAmount().doubleValue(), 0);
    // // assertEquals("LD-1", payouts.get(1).getGameId());
    // // assertEquals("GII-LD-1", payouts.get(1).getGameInstanceId());
    // // assertEquals(Payout.TYPE_WINNING, payouts.get(1).getType());
    // // assertEquals(Payout.STATUS_PAID, payouts.get(1).getStatus());
    // // }
    //
    // @Test
    // public void testPayout_ExceedPayoutLimit() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate.update("update BD_PRIZE_GROUP set max_value=1000 where id='BPG-1'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // PrizeDto prize = (PrizeDto) respCtx.getModel();
    //
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(SystemException.CODE_EXCEED_MAX_PAYOUT, respCtx.getResponseCode());
    // }
    //
    // @Test
    // public void testPayout_underPayoutLimit() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from ld_winning");
    // this.jdbcTemplate.update("update BD_PRIZE_GROUP set min_value=200000000,max_value=300000000 where id='BPG-1'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    // PrizeDto prize = (PrizeDto) respCtx.getModel();
    //
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(SystemException.CODE_EXCEED_MAX_PAYOUT, respCtx.getResponseCode());
    // }
    //
    // @Test
    // public void testPayout_CancelledTicket() throws Exception {
    // printMethod();
    // BingoTicket ticket = new BingoTicket();
    // ticket.setRawSerialNo("S-123456"); // print new ticket
    // ticket.setValidationCode("111111");
    // ticket.setPIN("PIN-111");
    // ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
    //
    // this.jdbcTemplate.update("update TE_BG_TICKET t set t.status=" + BaseTicket.STATUS_CANCELED
    // + " where t.serial_no='" + ticket.getSerialNo() + "'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(SystemException.CODE_INVALID_PAYOUT, respCtx.getResponseCode());
    // }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update BG_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
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

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
