package com.mpos.lottery.te.gameimpl.magic100.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.Magic100DomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Magic100SaleEnquiryIntegrationTest extends BaseServletIntegrationTest {

    // @Rollback(false)
    @Test
    public void testEnquiryTransaction() throws Exception {
        printMethod();

        // make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // enquiry the sale
        Transaction saleTrans = new Transaction();
        saleTrans.setDeviceId(saleCtx.getTerminalId());
        saleTrans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context enquiryCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), saleTrans);
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, enquiryRespCtx.getResponseCode());
        Transaction hostTrans = (Transaction) enquiryRespCtx.getModel();

        assertEquals(TransactionType.SELL_TICKET.getRequestType(), hostTrans.getType());
        Magic100Ticket hostTicket = (Magic100Ticket) hostTrans.getTicket();
        assertEquals(soldTicket.getSerialNo(), hostTicket.getSerialNo());
        assertEquals(soldTicket.getTotalAmount().doubleValue(), hostTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_PAID, hostTicket.getStatus());
        assertEquals(soldTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, hostTicket.getTicketType());
        assertTrue(hostTicket.isWinning());
        assertEquals(soldTicket.getValidationCode(), hostTicket.getValidationCode());
        assertEquals(soldTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(soldTicket.getTotalBets(), hostTicket.getTotalBets());

        assertEquals(3, hostTicket.getEntries().size());
        assertEquals("6", hostTicket.getEntries().get(0).getSelectNumber());
        assertEquals(false, ((Magic100Entry) hostTicket.getEntries().get(0)).isWinning());
        assertEquals("7", hostTicket.getEntries().get(1).getSelectNumber());
        assertEquals(false, ((Magic100Entry) hostTicket.getEntries().get(1)).isWinning());
        assertEquals("8", hostTicket.getEntries().get(2).getSelectNumber());
        assertEquals(true, ((Magic100Entry) hostTicket.getEntries().get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) hostTicket.getEntries().get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) hostTicket.getEntries().get(2)).getTaxAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiryTicket() throws Exception {
        printMethod();

        // make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // enquiry the sale
        ticket.setRawSerialNo(soldTicket.getRawSerialNo());
        Context enquiryCtx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        enquiryCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, enquiryRespCtx.getResponseCode());
        Magic100Ticket hostTicket = (Magic100Ticket) enquiryRespCtx.getModel();
        assertEquals(soldTicket.getSerialNo(), hostTicket.getSerialNo());
        assertEquals(soldTicket.getTotalAmount().doubleValue(), hostTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_PAID, hostTicket.getStatus());
        assertEquals(soldTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, hostTicket.getTicketType());
        assertTrue(hostTicket.isWinning());
        assertEquals(soldTicket.getValidationCode(), hostTicket.getValidationCode());
        assertEquals(soldTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(soldTicket.getTotalBets(), hostTicket.getTotalBets());

        assertEquals(3, hostTicket.getEntries().size());
        assertEquals("6", hostTicket.getEntries().get(0).getSelectNumber());
        assertEquals(false, ((Magic100Entry) hostTicket.getEntries().get(0)).isWinning());
        assertEquals("7", hostTicket.getEntries().get(1).getSelectNumber());
        assertEquals(false, ((Magic100Entry) hostTicket.getEntries().get(1)).isWinning());
        assertEquals("8", hostTicket.getEntries().get(2).getSelectNumber());
        assertEquals(true, ((Magic100Entry) hostTicket.getEntries().get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) hostTicket.getEntries().get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) hostTicket.getEntries().get(2)).getTaxAmount().doubleValue(), 0);
    }

    // --------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------
    private void sortEntries(List<Magic100Entry> entries) {
        Collections.sort(entries, new Comparator<Magic100Entry>() {

            @Override
            public int compare(Magic100Entry o1, Magic100Entry o2) {
                return (int) (o1.getSequenceOfNumber() - o2.getSequenceOfNumber());
            }

        });
    }
}
