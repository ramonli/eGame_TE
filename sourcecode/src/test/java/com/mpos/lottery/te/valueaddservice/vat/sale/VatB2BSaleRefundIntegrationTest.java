package com.mpos.lottery.te.valueaddservice.vat.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.VatDomainMocker;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class VatB2BSaleRefundIntegrationTest extends BaseServletIntegrationTest {
    private static Log logger = LogFactory.getLog(VatB2BSaleRefundIntegrationTest.class);
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;
    @Resource(name = "vatOperatorBalanceDao")
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;

    @Test
    public void test_Cancel_Magic100Sale_SingleBet_RoundUp_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setBuyerTaxNo("TAX-112");

        // set to bug magic100
        this.jdbcTemplate.update("update VAT_GAME set GAME_ID='LK-1' where ID='VG-1'");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        Magic100Ticket respTicket = (Magic100Ticket) respVatTrans.getTicket();
        assertEquals(SystemException.CODE_OK, saleRespCtx.getResponseCode());

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        VatSaleTransaction targetVatTrans = new VatSaleTransaction();
        targetVatTrans.setVatRefNo(respVatTrans.getVatRefNo());
        Context refundReqCtx = this.getDefaultContext(TransactionType.VAT_REFUND.getRequestType(), targetVatTrans);
        Context refundRespCtx = doPost(this.mockRequest(refundReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, refundRespCtx.getResponseCode());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().doubleValue(), newBalance.getSaleBalance().doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert cancellation trans
        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, refundRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(refundRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(refundReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(refundReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(refundReqCtx.getTraceMessageId());
        expectTrans.setType(refundReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbCancelTrans);

        // assert db tickets
        List<Magic100Ticket> hostTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());

        // assert vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(VatSaleTransaction.STATUS_INVALID, vatSaleTrans.getStatus());
    }

    @Rollback(true)
    @Test
    public void testCancel_RaffleSale_SingleBet_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setBuyerTaxNo("TAX-112");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);
        logger.debug("------ oldBalance(" + oldBalance + ").");

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        RaffleTicket respTicket = (RaffleTicket) respVatTrans.getTicket();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        VatSaleTransaction targetVatTrans = new VatSaleTransaction();
        targetVatTrans.setVatRefNo(respVatTrans.getVatRefNo());
        Context refundReqCtx = this.getDefaultContext(TransactionType.VAT_REFUND.getRequestType(), targetVatTrans);
        Context refundRespCtx = doPost(this.mockRequest(refundReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, refundRespCtx.getResponseCode());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().doubleValue(), newBalance.getSaleBalance().doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert cancellation trans
        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, refundRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(refundRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(refundReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(refundReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(refundReqCtx.getTraceMessageId());
        expectTrans.setType(refundReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbCancelTrans);

        // assert db tickets
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());

        // assert vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(VatSaleTransaction.STATUS_INVALID, vatSaleTrans.getStatus());
    }

    @Test
    public void testCancel_RaffleSale_NoTicket_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setBuyerTaxNo("TAX-112");

        // set to B2C
        this.jdbcTemplate.update("update VAT_GAME set MINIMUM_AMOUNT=9999999999");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        assertNull(respVatTrans.getTicket());

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        VatSaleTransaction targetVatTrans = new VatSaleTransaction();
        targetVatTrans.setVatRefNo(respVatTrans.getVatRefNo());
        Context refundReqCtx = this.getDefaultContext(TransactionType.VAT_REFUND.getRequestType(), targetVatTrans);
        Context refundRespCtx = doPost(this.mockRequest(refundReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, refundRespCtx.getResponseCode());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().doubleValue(), newBalance.getSaleBalance().doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert cancellation trans
        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, refundRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(refundRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(new BigDecimal("0"));
        expectTrans.setTicketSerialNo(null);
        expectTrans.setOperatorId(refundReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(refundReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(refundReqCtx.getTraceMessageId());
        expectTrans.setType(refundReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbCancelTrans);

        // // assert db tickets
        // List<Magic100Ticket> hostTickets =
        // this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
        // respTicket.getSerialNo(), false);
        // assertEquals(BaseTicket.STATUS_CANCELED,
        // hostTickets.get(0).getStatus());

        // assert vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(VatSaleTransaction.STATUS_INVALID, vatSaleTrans.getStatus());
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // ----------------------------------------------------------

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

    public VatSaleTransactionDao getVatSaleTransactionDao() {
        return vatSaleTransactionDao;
    }

    public void setVatSaleTransactionDao(VatSaleTransactionDao vatSaleTransactionDao) {
        this.vatSaleTransactionDao = vatSaleTransactionDao;
    }

    public VatOperatorBalanceDao getVatOperatorBalanceDao() {
        return vatOperatorBalanceDao;
    }

    public void setVatOperatorBalanceDao(VatOperatorBalanceDao vatOperatorBalanceDao) {
        this.vatOperatorBalanceDao = vatOperatorBalanceDao;
    }

    public VatDao getVatDao() {
        return vatDao;
    }

    public void setVatDao(VatDao vatDao) {
        this.vatDao = vatDao;
    }

}
