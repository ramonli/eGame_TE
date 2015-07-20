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
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.VatDomainMocker;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

public class VatB2CSaleAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testSaleMagic100_SingleBet_RoundUp_OK() throws Exception {
        VatSaleTransaction clientVatTrans = VatDomainMocker.mockVatSaleTransaction();
        clientVatTrans.setBuyerTaxNo("TAX-112");

        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        request.setGameTypeId(GameType.VAT.getType() + "");
        request.setModel(clientVatTrans);
        request.setTransType(TransactionType.SELL_TICKET.getRequestType());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        assertNotNull(response.getTransactionID());
        VatSaleTransaction respDto = (VatSaleTransaction) response.getModel();
        assertEquals(AirtimeTopup.STATUS_SUCCESS, respDto.getStatus());
    }

    // @Test
    // public void testEnquiryRaffle() throws Exception {
    // // enquiry ticket
    // RaffleTicket ticket = new RaffleTicket();
    // ticket.setRawSerialNo(false, "01011725418500500187");
    // Context request = this.mockRequestContext();
    // request.setWorkingKey(this.getDefaultWorkingKey());
    // request.setGameTypeId(GameType.RAFFLE.getType() + "");
    // request.setModel(ticket);
    // request.setTransType(TransactionType.TICKET_ENQUIRY.getRequestType());
    //
    // Context response = this.post(request);
    // assertEquals(200, response.getResponseCode());
    // }

}
