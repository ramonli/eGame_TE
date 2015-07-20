package com.mpos.lottery.te.gameimpl.extraball.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.List;

public class Ticket_Enquiry_IntegrationTest extends BaseServletIntegrationTest {
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;

    @Test
    public void testEnquiry_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-1");

        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        ExtraBallTicket respTicket = (ExtraBallTicket) respCtx.getModel();
        List<ExtraBallTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                respTicket.getSerialNo(), true);
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        assertEquals(respTicket.getMultipleDraws(), dbTickets.size());
        assertEquals(respTicket.getTotalAmount().divide(new BigDecimal(respTicket.getMultipleDraws())).doubleValue(),
                boughtTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("GAME-EB", respTicket.getGameInstance().getGame().getId());
        assertEquals("20120709", respTicket.getGameInstance().getNumber());

        // assert entries
        List<ExtraBallEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(ExtraBallEntry.class,
                respTicket.getSerialNo(), true);
        for (int i = 0; i < dbEntries.size(); i++) {
            ExtraBallEntry respEntry = (ExtraBallEntry) respTicket.getEntries().get(i);
            ExtraBallEntry dbEntry = dbEntries.get(i);
            assertEquals(dbEntry.getBetOption(), respEntry.getBetOption());
            assertEquals(dbEntry.getInputChannel(), respEntry.getInputChannel());
            assertEquals(dbEntry.getSelectNumber(), respEntry.getSelectNumber());
            assertEquals(dbEntry.getEntryAmount().doubleValue(), respEntry.getEntryAmount().doubleValue(), 0);
        }
    }

    @Test
    public void testEnquiry_NoNExist() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-NONEXIST");

        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(315, respCtx.getResponseCode());
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(@Qualifier("baseTicketDao") BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

}
