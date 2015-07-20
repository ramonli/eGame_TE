package com.mpos.lottery.te.gameimpl.lotto.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TicketEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryByTicket() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        LottoTicket saleRespTicket = (LottoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry ticket
        ticket.setRawSerialNo(saleRespTicket.getRawSerialNo());
        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert ticket
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_ACCEPTED, respTicket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), respTicket.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(16, respTicket.getTotalBets());
        assertEquals(saleRespTicket.getTotalBets(), respTicket.getTotalBets());
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(2, respTicket.getEntries().size());
        LottoEntry entry0 = (LottoEntry) respTicket.getEntries().get(0);
        assertEquals(100.0, entry0.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        LottoEntry entry1 = (LottoEntry) respTicket.getEntries().get(1);
        assertEquals(700.0, entry1.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(1).getSelectNumber(), entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());
    }

    @Rollback(true)
    @Test
    public void testEnquiryByTicket_PlayerSpecifyEntryAmount() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        BaseEntry customAmountSingleEntry = (BaseEntry) ticket.getEntries().get(0).clone();
        customAmountSingleEntry.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(customAmountSingleEntry);
        BaseEntry customAmountMultiEntry = (BaseEntry) ticket.getEntries().get(1).clone();
        customAmountMultiEntry.setEntryAmount(new BigDecimal("1400.0"));
        ticket.getEntries().add(customAmountMultiEntry);
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("5000"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        LottoTicket saleRespTicket = (LottoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry ticket
        ticket.setRawSerialNo(saleRespTicket.getRawSerialNo());
        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert ticket
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_ACCEPTED, respTicket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), respTicket.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), respTicket.getTotalBets());
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        List entries = respTicket.getEntries();
        assertEquals(4, entries.size());
        this.sortTicketEntriesByAmount(entries);
        LottoEntry entry = (LottoEntry) respTicket.getEntries().get(0);
        assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
        assertEquals("1,2,3,6,7,13", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(1);
        assertEquals(300.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
        assertEquals("1,2,3,6,7,13", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(2);
        assertEquals(700.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, entry.getBetOption());
        assertEquals("3,11,14,16,22,25,36", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(3);
        assertEquals(1400.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, entry.getBetOption());
        assertEquals("3,11,14,16,22,25,36", entry.getSelectNumber());
    }

    @Rollback(true)
    @Test
    public void testEnquiryByTrans_PlayerSpecifyEntryAmount() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        BaseEntry customAmountSingleEntry = (BaseEntry) ticket.getEntries().get(0).clone();
        customAmountSingleEntry.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(customAmountSingleEntry);
        BaseEntry customAmountMultiEntry = (BaseEntry) ticket.getEntries().get(1).clone();
        customAmountMultiEntry.setEntryAmount(new BigDecimal("1400.0"));
        ticket.getEntries().add(customAmountMultiEntry);
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("5000"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        LottoTicket saleRespTicket = (LottoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry transaction
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context ctx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(ctx));
        Transaction respTrans = (Transaction) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert trans
        assertEquals(saleRespCtx.getTransactionID(), respTrans.getId());
        assertEquals(SystemException.CODE_OK, respTrans.getResponseCode());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), respTrans.getType());
        // assert ticket
        LottoTicket respTicket = (LottoTicket) respTrans.getTicket();
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(saleRespTicket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_ACCEPTED, respTicket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), respTicket.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), respTicket.getTotalBets());
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        List entries = respTicket.getEntries();
        assertEquals(4, entries.size());
        this.sortTicketEntriesByAmount(entries);
        LottoEntry entry = (LottoEntry) respTicket.getEntries().get(0);
        assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
        assertEquals("1,2,3,6,7,13", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(1);
        assertEquals(300.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
        assertEquals("1,2,3,6,7,13", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(2);
        assertEquals(700.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, entry.getBetOption());
        assertEquals("3,11,14,16,22,25,36", entry.getSelectNumber());
        entry = (LottoEntry) respTicket.getEntries().get(3);
        assertEquals(1400.0, entry.getEntryAmount().doubleValue(), 0);
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, entry.getBetOption());
        assertEquals("3,11,14,16,22,25,36", entry.getSelectNumber());
    }

    @Test
    public void testEnquiryByTrans() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        LottoTicket saleRespTicket = (LottoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry transaction
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context ctx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(ctx));
        Transaction respTrans = (Transaction) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());
        // assert trans
        assertEquals(saleRespCtx.getTransactionID(), respTrans.getId());
        assertEquals(SystemException.CODE_OK, respTrans.getResponseCode());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), respTrans.getType());
        // assert ticket
        LottoTicket ticketDto = (LottoTicket) respTrans.getTicket();
        assertEquals(saleRespTicket.getRawSerialNo(), ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.STATUS_ACCEPTED, ticketDto.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), ticketDto.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), ticketDto.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), ticketDto.getTotalBets());
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        BaseGameInstance gameInstance = ticketDto.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(2, ticketDto.getEntries().size());
        LottoEntry entry0 = (LottoEntry) ticketDto.getEntries().get(0);
        assertEquals(100.0, entry0.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        LottoEntry entry1 = (LottoEntry) ticketDto.getEntries().get(1);
        assertEquals(700.0, entry1.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(1).getSelectNumber(), entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());
    }

    @SuppressWarnings("unchecked")
    protected void sortTicketEntriesByAmount(List entries) {
        Collections.sort(entries, new Comparator<BaseEntry>() {

            @Override
            public int compare(BaseEntry o1, BaseEntry o2) {
                return o1.getEntryAmount().compareTo(o2.getEntryAmount());
            }

        });
    }
}
