package com.mpos.lottery.te.gameimpl.instantgame;

import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class BatchValidationVIRNIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "instantTicketDao")
    private InstantTicketDao instantTicketDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "payoutDetailDao")
    private PayoutDetailDao payoutDetailDao;

    @Test
    public void testBatchValidate_CASH_OBJECT_AllSuccess() throws Exception {
        this.printMethod();
        // InstantBatchPayoutDto batchPayout = this.mock();
        //
        // this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // this.jdbcTemplate.update("update INSTANT_TICKET set STATUS=3 where TICKET_SERIAL='"
        // + batchPayout.getPayouts().get(1).getTicket().getSerialNo() + "'");
        //
        // Context reqCtx = this.getDefaultContext(TransactionType.BATCH_VALIDATION.getRequestType(),
        // batchPayout);
        // Context respCtx = this.doPost(this.mockRequest(reqCtx));
        //
        // // assert response
        // assertEquals(200, respCtx.getResponseCode());
        // InstantBatchPayoutDto respDto = (InstantBatchPayoutDto) respCtx.getModel();
        // assertEquals(0, respDto.getTotalFail());
        // assertEquals(2, respDto.getTotalSuccess());
        // assertEquals(2, respDto.getPayouts().size());
        // assertEquals(new BigDecimal("4000"), respDto.getTaxAmount());
        // assertEquals(new BigDecimal("40000"), respDto.getActualAmount());
        //
        // PrizeLevelDto p0 = respDto.getPayouts().get(0);
        // assertEquals(SystemException.CODE_OK, p0.getErrorCode());
        // assertEquals(30000.0, p0.getActualAmount().doubleValue(), 0);
        // assertEquals(31111.0, p0.getPrizeAmount().doubleValue(), 0);
        // assertEquals(3000.0, p0.getTaxAmount().doubleValue(), 0);
        //
        // PrizeLevelDto p1 = respDto.getPayouts().get(1);
        // assertEquals(SystemException.CODE_OK, p1.getErrorCode());
        // assertEquals(10000.0, p1.getActualAmount().doubleValue(), 0);
        // assertEquals(11111.0, p1.getPrizeAmount().doubleValue(), 0);
        // assertEquals(1000.0, p1.getTaxAmount().doubleValue(), 0);
    }

    @Test
    public void testBatchValidate_CASH_OBJECT_PartialSuccess() throws Exception {
        this.printMethod();
        // InstantBatchPayoutDto batchPayout = this.mock();
        //
        // this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        //
        // Context reqCtx = this.getDefaultContext(TransactionType.BATCH_VALIDATION.getRequestType(),
        // batchPayout);
        // Context respCtx = this.doPost(this.mockRequest(reqCtx));
        //
        // // assert response
        // assertEquals(200, respCtx.getResponseCode());
        // InstantBatchPayoutDto respDto = (InstantBatchPayoutDto) respCtx.getModel();
        // assertEquals(1, respDto.getTotalFail());
        // assertEquals(1, respDto.getTotalSuccess());
        // assertEquals(2, respDto.getPayouts().size());
        // assertEquals(new BigDecimal("3000"), respDto.getTaxAmount());
        // assertEquals(new BigDecimal("30000"), respDto.getActualAmount());
        //
        // PrizeLevelDto p0 = respDto.getPayouts().get(0);
        // assertEquals(SystemException.CODE_OK, p0.getErrorCode());
        // assertEquals(30000.0, p0.getActualAmount().doubleValue(), 0);
        // assertEquals(31111.0, p0.getPrizeAmount().doubleValue(), 0);
        // assertEquals(3000.0, p0.getTaxAmount().doubleValue(), 0);
        //
        // PrizeLevelDto p1 = respDto.getPayouts().get(1);
        // assertEquals(SystemException.CODE_VALIDATE_NOACTIVETICKET, p1.getErrorCode());
    }

    protected InstantBatchPayoutDto mock() {
        InstantBatchPayoutDto batchPayout = new InstantBatchPayoutDto();

        InstantTicket t1 = new InstantTicket();
        t1.setRawSerialNo("198415681983");
        t1.setTicketXOR3("27330200");
        PrizeLevelDto p1 = new PrizeLevelDto();
        p1.setClientPrizeAmount(new BigDecimal("31111"));
        p1.setTicket(t1);
        batchPayout.getPayouts().add(p1);

        InstantTicket t2 = new InstantTicket();
        t2.setRawSerialNo("198415681002");
        t2.setTicketXOR3("37330218");
        PrizeLevelDto p2 = new PrizeLevelDto();
        p2.setClientPrizeAmount(new BigDecimal("11111"));
        p2.setTicket(t2);
        batchPayout.getPayouts().add(p2);

        return batchPayout;
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

}
