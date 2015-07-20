package com.mpos.lottery.te.gameimpl.lfn.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.LfnDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;
import com.mpos.lottery.te.gamespec.sale.dao.RiskControlLogDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;

import javax.annotation.Resource;

public class LfnSale_RiskControl_IntegrationTest extends BaseServletIntegrationTest {
    private Log logger = LogFactory.getLog(LfnSale_RiskControl_IntegrationTest.class);
    @Resource(name = "riskControlLogDao")
    private RiskControlLogDao riskControlLogDao;

    /**
     * As the generation of risk control log is in a independent transaction, we must prepare them before each test.
     */
    @BeforeTransaction
    public void prepareRiskControlLog() throws Exception {
        super.prepareRiskControlLog();
        String sql2 = "insert into BD_RISK_BETTING(ID,BETTING_NUMBER,GAME_INSTANCE_ID,TOTAL_AMOUNT,PRIZE_LEVEL_TYPE) "
                + "values('LFN-1', '5', 'GII-112', 100,1)";
        this.executeSqlInNewTransaction(sql2);
    }

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_DynamicRisk_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(1).setSelectNumber("2,7,13,24");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert risk control log
        RiskControlLog dbLog = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "5", 1);
        assertEquals(200, dbLog.getTotalAmount().doubleValue(), 0);
        dbLog = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "5", 1);
        assertEquals(100, dbLog.getTotalAmount().doubleValue(), 0);

        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,7,13", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,7,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "7,13,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,13,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);

        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,7,13", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,7,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "7,13,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,13,24", 3);
        assertEquals(300.0, log.getTotalAmount().doubleValue(), 0);
    }

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_DynamicRisk_Cancell_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(1).setSelectNumber("2,7,13,24");

        // 1. make sale
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // 2. cancel
        LfnTicket cancelTicket = new LfnTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert risk control log
        RiskControlLog dbLog = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "5", 1);
        assertEquals(100, dbLog.getTotalAmount().doubleValue(), 0);
        dbLog = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "5", 1);
        assertEquals(0, dbLog.getTotalAmount().doubleValue(), 0);

        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,7,13", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,7,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "7,13,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,13,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);

        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,7,13", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,7,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "7,13,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,13,24", 3);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
    }

    @Test
    public void testSell_MultiDraw_DynamicRisk_Fail_FixAmount() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update lfn_game_instance set LOSS_AMOUNT=4000");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OUT_OF_RISK_CONTROL, respCtx.getResponseCode());
    }

    /**
     * THe risk prize amount exceeds the percentage of turnover.
     */
    @Test
    public void testSell_MultiDraw_DynamicRisk_Fail_Turnover() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // make sure the lossAmount is less than percentage of turnover
        this.jdbcTemplate.update("update lfn_game_instance set LOSS_AMOUNT=3000");
        this.jdbcTemplate.update("update lfn_game_instance set SALES_AMOUNT_PERCENT=0.1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OUT_OF_RISK_CONTROL, respCtx.getResponseCode());
    }

    @Test
    public void testSell_MultiDraw_FixLossRisk_Fail() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update lfn_game_instance set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update lfn_game_instance set LOSS_AMOUNT=4000");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OUT_OF_RISK_CONTROL, respCtx.getResponseCode());
    }

    public RiskControlLogDao getRiskControlLogDao() {
        return riskControlLogDao;
    }

    public void setRiskControlLogDao(RiskControlLogDao riskControlLogDao) {
        this.riskControlLogDao = riskControlLogDao;
    }

}
