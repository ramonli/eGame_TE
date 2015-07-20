package com.mpos.lottery.te.valueaddservice.vat;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.OfflineCancellation;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.OfflinecancellationDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.SelectedNumberPackDto;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class DownloadOfflineTicketCancellationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;

    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;

    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Resource(name = "offlinecancellationDao")
    private OfflinecancellationDao offlinecancellationDao;

    @Test
    public void testCancelByTransaction_OK() throws Exception {
        printMethod();
        OfflineTicketPackDto dto = mockDto();

        this.jdbcTemplate
                .update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID=2 where operator_id='OPERATOR-111'");

        this.jdbcTemplate.update("update lk_offline_cancellation set is_handled=1 where game_id='LK-1'");

        Context ctx = this.getDefaultContext(TransactionType.RESERVED_NUMBERS.getRequestType(), dto);
        // ctx.setGameTypeId(String.valueOf(GameType.LUCKYNUMBER.getType()));//set
        // vat raffle -14
        Context respCtx = doPost(this.mockRequest(ctx));
        OfflineTicketPackDto respDto = (OfflineTicketPackDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert ticket
        List<OfflineCancellation> hostOfflineCancellations = offlinecancellationDao.findByTransactionId(respCtx
                .getTransactionID());
        OfflineCancellation offlineCancellation = hostOfflineCancellations.get(0);

        assertEquals(1, hostOfflineCancellations.size());
        assertEquals(5, offlineCancellation.getStartNumber());
        assertEquals(4, offlineCancellation.getEndNumber());
        assertEquals(4, offlineCancellation.getCurrentNumber());

        // 2. make cancellation
        Transaction trans1 = new Transaction();
        trans1.setDeviceId(ctx.getTerminalId());
        trans1.setTraceMessageId(ctx.getTraceMessageId());
        cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_CANCELLED_TRANS, cancelRespCtx.getResponseCode());
    }

    private void triggerCancelDecline() {
        this.jdbcTemplate
                .update("update TOTO_GAME_INSTANCE set game_freezing_time=sysdate-30/(24*60) where GAME_INSTANCE_ID='GII-112'");
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

    private OfflineTicketPackDto mockDto() {
        OfflineTicketPackDto offlineTicketPackDto = new OfflineTicketPackDto();
        SelectedNumberPackDto selectedNumberPackDto = new SelectedNumberPackDto();
        selectedNumberPackDto.setRequestCount(10);
        offlineTicketPackDto.setSelectedNumberPackDto(selectedNumberPackDto);
        offlineTicketPackDto.setVat(mockVat());
        return offlineTicketPackDto;
    }

    public static VAT mockVat() {
        VAT vat = new VAT();
        vat.setCode("foodA");
        return vat;
    }

    /**
     * @return return offlinecancellationDao
     */
    public OfflinecancellationDao getOfflinecancellationDao() {
        return offlinecancellationDao;
    }

    /**
     * @param assiging
     *            offlinecancellationDao
     */
    public void setOfflinecancellationDao(OfflinecancellationDao offlinecancellationDao) {
        this.offlinecancellationDao = offlinecancellationDao;
    }
}
