package com.mpos.lottery.te.gameimpl.toto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.toto.TotoDomainMocker;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToGameInstance;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class SaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Rollback(true)
    @Test
    public void testSale_OK() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));
        User user = new User();
        user.setCreditCardSN("CC-12345678");
        ticket.setUser(user);

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        // force to flush to underlying database to avoid FALSE-POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // TODO assert the response
        assertEquals(6, respTicket.getTotalBets());
        assertEquals(1, respTicket.getMultipleDraws());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(new Barcoder(Game.TYPE_TOTO, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<ToToTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        ToToTicket expectTicket = new ToToTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(6);
        expectTicket.setBarcode(respTicket.getBarcode());
        expectTicket.setValidationCode(respTicket.getValidationCode());
        ToToGameInstance gameInstance = new ToToGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));

        // asert entries
        List<ToToEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(ToToEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, dbEntries.size());
        ToToEntry dbEntry1 = dbEntries.get(0);
        assertEquals(600.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(6, dbEntry1.getTotalBets());
        assertEquals(0, dbEntry1.getBetOption());
    }

    /**
     * The selected number must be 0,1,2
     */
    @Rollback(true)
    @Test
    public void testSale_IllegalSelectedNumber() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ((ToToEntry) ticket.getEntries().get(0)).setSelectNumber("0,1,2");
        ticket.setTotalAmount(new BigDecimal("100.0"));
        User user = new User();
        user.setCreditCardSN("CC-12345678");
        ticket.setUser(user);

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        assertEquals(SystemException.CODE_TOTO_SELECT_TEAM_IS_ERROR, respCtx.getResponseCode());
    }

    /**
     * Verify the rule of triple and double
     */
    @Test
    public void testSale_TripleDoubleRule() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));
        User user = new User();
        user.setCreditCardSN("CC-12345678");
        ticket.setUser(user);

        this.jdbcTemplate.update("update TT_OPERATION_PARAMETERS set min_double=2 where seq_number=2");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        assertEquals(SystemException.CODE_TOTO_TRIPLE_INFO_ERROR, respCtx.getResponseCode());
    }

    @Test
    public void testSale_GameInstanceSuspendSale() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));
        User user = new User();
        user.setCreditCardSN("CC-12345678");
        ticket.setUser(user);

        this.jdbcTemplate.update("update TOTO_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }
}
