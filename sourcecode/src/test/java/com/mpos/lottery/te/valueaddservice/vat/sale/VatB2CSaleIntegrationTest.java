package com.mpos.lottery.te.valueaddservice.vat.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
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

public class VatB2CSaleIntegrationTest extends BaseServletIntegrationTest {
    private static Log logger = LogFactory.getLog(VatB2BSaleCancellationIntegrationTest.class);
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;
    @Resource(name = "vatOperatorBalanceDao")
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;

    @Test
    public void testSaleRaffle_SingleBet_RoundUp_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");
        this.jdbcTemplate.update("update VAT_GAME set GAME_ID='RA-1' where id='VG-2'");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        RaffleTicket respTicket = (RaffleTicket) respVatTrans.getTicket();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, saleRespCtx.getResponseCode());
        assertEquals(GameType.RAFFLE.getType(), saleRespCtx.getGameTypeIdIntValue());
        // assert response DTO
        assertNotNull(respVatTrans.getVatRefNo());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), respVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatCode(), respVatTrans.getVatCode());
        assertEquals(0.1, respVatTrans.getVatRate().doubleValue(), 0);
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("11002", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(GameType.RAFFLE.getType(), respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(1, respTicket.getTotalBets());
        assertEquals(0, respTicket.getEntries().size());
        assertEquals(100.0, respTicket.getTotalAmount().doubleValue(), 0);
        // game instance
        assertEquals("11002", respTicket.getGameInstance().getNumber());
        assertEquals("RA-1", respTicket.getGameInstance().getGameId());
        assertEquals(GameType.RAFFLE.getType(), respTicket.getGameInstance().getGameType().intValue());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().add(respVatTrans.getVatTotalAmount()).doubleValue(), newBalance
                .getSaleBalance().doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);
        assertEquals(GameType.RAFFLE.getType() * -1, dbTrans.getVersion());

        // assert db tickets
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());

        // assert vat sale transaction
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(respVatTrans.getVatRefNo(), hostVatTrans.getVatRefNo());
        assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
        assertNull(hostVatTrans.getSellerCompanyId());
        assertNull(hostVatTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(respTicket.getTotalAmount().doubleValue(), hostVatTrans.getVatRateTotalAmount().doubleValue(), 0);
        assertEquals(respTicket.getSerialNo(), hostVatTrans.getTicketSerialNo());
        assertEquals("GII-112", hostVatTrans.getGameInstanceId());
        assertEquals(GameType.RAFFLE.getType(), hostVatTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
        assertEquals(respTicket.getTotalAmount().doubleValue(), hostVatTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, hostVatTrans.getBusinessType());
    }

    @Rollback(true)
    @Test
    public void testSaleMagi100_SingleBet_RoundUp_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        Magic100Ticket respTicket = (Magic100Ticket) respVatTrans.getTicket();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, saleRespCtx.getResponseCode());
        assertEquals(GameType.LUCKYNUMBER.getType(), saleRespCtx.getGameTypeIdIntValue());
        // assert response DTO
        assertNotNull(respVatTrans.getVatRefNo());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), respVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatCode(), respVatTrans.getVatCode());
        assertEquals(0.1, respVatTrans.getVatRate().doubleValue(), 0);
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("001", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(GameType.LUCKYNUMBER.getType(), respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(1, respTicket.getTotalBets());
        assertEquals(1, respTicket.getEntries().size());
        assertEquals(100.0, respTicket.getTotalAmount().doubleValue(), 0);
        // game instance
        assertEquals("001", respTicket.getGameInstance().getNumber());
        assertEquals("LK-1", respTicket.getGameInstance().getGameId());
        assertEquals(GameType.LUCKYNUMBER.getType(), respTicket.getGameInstance().getGameType().intValue());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().add(respVatTrans.getVatTotalAmount()).doubleValue(), newBalance
                .getSaleBalance().doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);
        assertEquals(GameType.LUCKYNUMBER.getType() * -1, dbTrans.getVersion());

        // assert db tickets
        List<Magic100Ticket> hostTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());

        // assert db entries
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, dbEntries.size());

        // assert vat sale transaction
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(respVatTrans.getVatRefNo(), hostVatTrans.getVatRefNo());
        assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
        assertNull(hostVatTrans.getSellerCompanyId());
        assertNull(hostVatTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatTotalAmount().multiply(respVatTrans.getVatRate()).doubleValue(), hostVatTrans
                .getVatRateTotalAmount().doubleValue(), 0);
        assertEquals(respTicket.getSerialNo(), hostVatTrans.getTicketSerialNo());
        assertEquals("GII-111", hostVatTrans.getGameInstanceId());
        assertEquals(GameType.LUCKYNUMBER.getType(), hostVatTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
        assertEquals(respTicket.getTotalAmount().doubleValue(), hostVatTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, hostVatTrans.getBusinessType());
    }

    @Test
    public void testSaleMagic100_MultiBets_RoundUp_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setVatTotalAmount(new BigDecimal("4700"));

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        Magic100Ticket respTicket = (Magic100Ticket) respVatTrans.getTicket();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, saleRespCtx.getResponseCode());
        assertEquals(GameType.LUCKYNUMBER.getType(), saleRespCtx.getGameTypeIdIntValue());
        // assert DTO
        assertNotNull(respVatTrans.getVatRefNo());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), respVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatCode(), respVatTrans.getVatCode());
        assertEquals(0.1, respVatTrans.getVatRate().doubleValue(), 0);
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("001", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(GameType.LUCKYNUMBER.getType(), respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(5, respTicket.getTotalBets());
        assertEquals(5, respTicket.getEntries().size());
        assertEquals(500.0, respTicket.getTotalAmount().doubleValue(), 0);
        // game instance
        assertEquals("001", respTicket.getGameInstance().getNumber());
        assertEquals("LK-1", respTicket.getGameInstance().getGameId());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().add(respVatTrans.getVatTotalAmount()).doubleValue(), newBalance
                .getSaleBalance().doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert db tickets
        List<Magic100Ticket> hostTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());

        // assert db entries
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(5, dbEntries.size());

        // assert vat sale transaction
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(respVatTrans.getVatRefNo(), hostVatTrans.getVatRefNo());
        assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
        assertNull(hostVatTrans.getSellerCompanyId());
        assertNull(hostVatTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatTotalAmount().multiply(respVatTrans.getVatRate()).doubleValue(), hostVatTrans
                .getVatRateTotalAmount().doubleValue(), 0);
        assertEquals(respTicket.getSerialNo(), hostVatTrans.getTicketSerialNo());
        assertEquals("GII-111", hostVatTrans.getGameInstanceId());
        assertEquals(GameType.LUCKYNUMBER.getType(), hostVatTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
        assertEquals(respTicket.getTotalAmount().doubleValue(), hostVatTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, hostVatTrans.getBusinessType());
    }

    @Test
    public void testSaleMagic100_MultiBets_RoundDown_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setVatTotalAmount(new BigDecimal("4700"));

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");
        this.jdbcTemplate.update("update VAT set round_is_up_down=1");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, saleRespCtx.getResponseCode());
        assertEquals(GameType.LUCKYNUMBER.getType(), saleRespCtx.getGameTypeIdIntValue());
        assertNotNull(respVatTrans.getVatRefNo());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), respVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatCode(), respVatTrans.getVatCode());
        assertEquals(0.1, respVatTrans.getVatRate().doubleValue(), 0);
        // assert ticket
        Magic100Ticket respTicket = (Magic100Ticket) respVatTrans.getTicket();
        assertNotNull(respVatTrans.getTicket());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("001", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(GameType.LUCKYNUMBER.getType(), respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(4, respTicket.getTotalBets());
        assertEquals(4, respTicket.getEntries().size());
        assertEquals(400.0, respTicket.getTotalAmount().doubleValue(), 0);
        // game instance
        assertEquals("001", respTicket.getGameInstance().getNumber());
        assertEquals("LK-1", respTicket.getGameInstance().getGameId());

        // assert vat sale balance
        VatOperatorBalance newBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        assertEquals(oldBalance.getSaleBalance().add(respVatTrans.getVatTotalAmount()).doubleValue(), newBalance
                .getSaleBalance().doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(respTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert db tickets
        List<Magic100Ticket> hostTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());

        // assert db entries
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(4, dbEntries.size());

        // assert vat sale transaction
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(respVatTrans.getVatRefNo(), hostVatTrans.getVatRefNo());
        assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
        assertNull(hostVatTrans.getSellerCompanyId());
        assertNull(hostVatTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(clientVatTrans.getVatTotalAmount().multiply(respVatTrans.getVatRate()).doubleValue(), hostVatTrans
                .getVatRateTotalAmount().doubleValue(), 0);
        assertEquals(respTicket.getSerialNo(), hostVatTrans.getTicketSerialNo());
        assertEquals("GII-111", hostVatTrans.getGameInstanceId());
        assertEquals(GameType.LUCKYNUMBER.getType(), hostVatTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
        assertEquals(respTicket.getTotalAmount().doubleValue(), hostVatTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, hostVatTrans.getBusinessType());
    }

    @Test
    public void testSaleMagic100_NoBet_RoundDown_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");
        this.jdbcTemplate.update("update VAT set round_is_up_down=1");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, saleRespCtx.getResponseCode());
        assertNull(respVatTrans.getTicket());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(new BigDecimal("0.0"));
        expectTrans.setTicketSerialNo(null);
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(dbTrans.getOperatorId(), vatSaleTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), vatSaleTrans.getMerchantId());
        assertNull(vatSaleTrans.getSellerCompanyId());
        assertNull(vatSaleTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), vatSaleTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(0.0, vatSaleTrans.getVatRateTotalAmount().doubleValue(), 0);
        assertNull(vatSaleTrans.getTicketSerialNo());
        assertNull(vatSaleTrans.getGameInstanceId());
        assertEquals(GameType.LUCKYNUMBER.getType(), vatSaleTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), vatSaleTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, vatSaleTrans.getStatus());
        assertEquals(0.0, vatSaleTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, vatSaleTrans.getBusinessType());
        assertEquals(respVatTrans.getVatRefNo(), vatSaleTrans.getVatRefNo());
    }

    @Test
    public void testSaleMagic100_NoBet_UnderMinThresholdAmount_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");
        this.jdbcTemplate.update("update VAT_GAME set MINIMUM_AMOUNT=9999999999");

        VatOperatorBalance oldBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111");
        this.entityManager.detach(oldBalance);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, saleRespCtx.getResponseCode());
        assertNull(respVatTrans.getTicket());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(new BigDecimal("0.0"));
        expectTrans.setTicketSerialNo(null);
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(
                saleRespCtx.getTransactionID());
        assertEquals(dbTrans.getOperatorId(), vatSaleTrans.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), vatSaleTrans.getMerchantId());
        assertNull(vatSaleTrans.getSellerCompanyId());
        assertNull(vatSaleTrans.getBuyerCompanyId());
        assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), vatSaleTrans.getVatTotalAmount().doubleValue(),
                0);
        assertEquals(0.0, vatSaleTrans.getVatRateTotalAmount().doubleValue(), 0);
        assertNull(vatSaleTrans.getTicketSerialNo());
        assertNull(vatSaleTrans.getGameInstanceId());
        assertEquals(GameType.LUCKYNUMBER.getType(), vatSaleTrans.getGameType());
        assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), vatSaleTrans.getVatId());
        assertEquals(VatSaleTransaction.STATUS_VALID, vatSaleTrans.getStatus());
        assertEquals(0.0, vatSaleTrans.getSaleTotalAmount().doubleValue(), 0);
        assertEquals(OperatorBizType.BIZ_B2C, vatSaleTrans.getBusinessType());
        assertEquals(respVatTrans.getVatRefNo(), vatSaleTrans.getVatRefNo());
    }

    // --------------------------------------------------------------
    // SPRINT DEPENDENCIES INEJCTION
    // --------------------------------------------------------------
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

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

}
