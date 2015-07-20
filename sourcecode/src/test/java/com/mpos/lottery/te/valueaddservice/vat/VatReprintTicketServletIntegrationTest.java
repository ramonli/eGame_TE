package com.mpos.lottery.te.valueaddservice.vat;

import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.web.VatReprintTicketReqDto;

import org.junit.Test;

public class VatReprintTicketServletIntegrationTest extends BaseServletIntegrationTest {
    // @Rollback(false)
    @Test
    public void testRaffleReprint() throws Exception {

        printMethod();
        VatReprintTicketReqDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.VAT_REPRINT_TICKET.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(GameType.RAFFLE.getType()));// set vat raffle 14
        Context respCtx = doPost(this.mockRequest(reqCtx));
        VatSaleTransaction respVatTrans = (VatSaleTransaction) respCtx.getModel();
        RaffleTicket respTicket = (RaffleTicket) respVatTrans.getTicket();
        
        // descrypt serialno
        // Barcoder barcode = new Barcoder(14,"S-123456");
        // System.out.println("barcode="+barcode.getBarcode());
        // String plainSerialNo = BaseTicket.descryptSerialNo("9pkxn/npytVqVbOF5fPlsg==");
        // System.out.println("plainSerialNo="+plainSerialNo);

        assertNotNull(respTicket);
    }

    // @Rollback(false)
    @Test
    public void testMagic100Reprint() throws Exception {

        printMethod();
        VatReprintTicketReqDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.VAT_REPRINT_TICKET.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(GameType.LUCKYNUMBER.getType()));// set vat magic 18
        Context respCtx = doPost(this.mockRequest(reqCtx));
        
        VatSaleTransaction respVatTrans = (VatSaleTransaction) respCtx.getModel();
        Magic100Ticket respTicket = (Magic100Ticket) respVatTrans.getTicket();

        assertNotNull(respTicket);
    }

    private VatReprintTicketReqDto mockDto() {
        VatReprintTicketReqDto dto = new VatReprintTicketReqDto();
        dto.setSerialNo("S-123456");
        return dto;
    }

}
