package com.mpos.lottery.te.valueaddservice.vat.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.Game;
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

public class VatB2CTicketEnquiryIntegrationTest extends BaseServletIntegrationTest {
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
    public void testSaleRaffle_Enqyiry_OK() throws Exception {
        this.printMethod();
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();

        // set to B2C
        this.jdbcTemplate.update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID='2'");
        this.jdbcTemplate.update("update VAT_GAME set GAME_ID='RA-1' where id='VG-2'");

        // 1st. make sale first
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientVatTrans);
        saleReqCtx.setGameTypeId(GameType.VAT.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) saleRespCtx.getModel();
        RaffleTicket respTicket = (RaffleTicket) respVatTrans.getTicket();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry ticket
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        Context enquiryCtx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        enquiryCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respEnquiryCtx = doPost(this.mockRequest(enquiryCtx));
        RaffleTicket respEnquiryTicket = (RaffleTicket) respEnquiryCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respEnquiryCtx.getResponseCode());
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

    }

    // --------------------------------------------------------------
    // SPRING DEPENDENCIES INEJCTION
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
