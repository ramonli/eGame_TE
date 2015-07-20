package com.mpos.lottery.te.gameimpl.digital.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.DigitalDomainMocker;
import com.mpos.lottery.te.gameimpl.digital.sale.support.DigitalRiskControlService;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
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

import java.math.BigDecimal;

import javax.annotation.Resource;

public class Sale_RiskControl_IntegrationTest extends BaseServletIntegrationTest {
    private Log logger = LogFactory.getLog(Sale_RiskControl_IntegrationTest.class);
    @Resource(name = "riskControlLogDao")
    private RiskControlLogDao riskControlLogDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    /**
     * As the generation of risk control log is in a independent transaction, we must prepare them before each test.
     */
    @BeforeTransaction
    public void prepareRiskControlLog() throws Exception {
        super.prepareRiskControlLog();
        String sql2 = "insert into BD_RISK_BETTING(ID,BETTING_NUMBER,GAME_INSTANCE_ID,TOTAL_AMOUNT,PRIZE_LEVEL_TYPE) "
                + "values('DIGITAL-1', '0,3,2,8', 'GII-112', 50,41)";
        String sql3 = "insert into BD_RISK_BETTING(ID,BETTING_NUMBER,GAME_INSTANCE_ID,TOTAL_AMOUNT,PRIZE_LEVEL_TYPE) "
                + "values('DIGITAL-2', '25', 'GII-112', 100,-3)";
        this.executeSqlInNewTransaction(sql2, sql3);
    }

    /**
     * Verify the risk of the 4D entry. TypeA only supports 4D.
     */
    @Rollback(true)
    @Test
    public void testSell_FixLossRisk_TypeA_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        // typeA only supports XD
        clientTicket.getEntries().remove(1);
        clientTicket.setTotalAmount(new BigDecimal("120"));

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_A
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=400000");
        this.jdbcTemplate.update("update BD_RISK_BETTING set total_amount=0 where ID='DIGITAL-2'");
        // only XD supported
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0 and PRIZE_LOGIC_ID='PL-DIG-111'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='13' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='12'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='23' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='22'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='33' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='32'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='43' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='42'");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-999','PL-DIG-111', '51',50,2,1, '5th exactly matched 4D')");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-998','PL-DIG-111', '53',53,2,1, '5th mixed 4D')");
        this.jdbcTemplate.update("update PRIZE_PARAMETERS set bet_option=1 where PRIZE_LOGIC_ID='PL-DIG-111'");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert risk control log
        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 11);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 13);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 21);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 23);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 31);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 33);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 41);
        assertEquals(110.0, log.getTotalAmount().doubleValue(), 0);
        // log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8",
        // 43);
        // assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 51);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 53);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        // GII-113
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 11);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 13);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 21);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 23);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 31);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 33);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 41);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        // log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8",
        // 43);
        // assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 51);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 53);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
    }

    /**
     * Verify the risk of the 4D entry. TypeA only supports 4D.
     */
    @Rollback(true)
    @Test
    public void testSell_FixLossRisk_TypeA_Cancel_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        // typeA only supports XD
        clientTicket.getEntries().remove(1);
        clientTicket.setTotalAmount(new BigDecimal("120"));

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_A
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=400000");
        this.jdbcTemplate.update("update BD_RISK_BETTING set total_amount=0 where ID='DIGITAL-2'");
        // only XD supported
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0 and PRIZE_LOGIC_ID='PL-DIG-111'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='13' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='12'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='23' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='22'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='33' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='32'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='43' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='42'");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-999','PL-DIG-111', '51',50,2,1, '5th exactly matched 4D')");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-998','PL-DIG-111', '53',53,2,1, '5th mixed 4D')");
        this.jdbcTemplate.update("update PRIZE_PARAMETERS set bet_option=1 where PRIZE_LOGIC_ID='PL-DIG-111'");

        // 1. make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // 2. cancel by ticket
        DigitalTicket cancelTicket = new DigitalTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert risk control log
        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 11);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 13);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 21);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 23);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 31);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 33);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 41);
        assertEquals(50.0, log.getTotalAmount().doubleValue(), 0);
        // log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8",
        // 43);
        // assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 51);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,2,3,8", 53);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        // GII-113
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 11);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 13);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 21);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 23);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 31);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 33);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 41);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        // log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8",
        // 43);
        // assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 51);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,2,3,8", 53);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
    }

    /**
     * Verify the risk of the 4D entry. TypeA only supports 4D.
     */
    @Rollback(true)
    @Test
    public void testSell_FixLossRisk_TypeA_Fail() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        // typeA only supports XD
        clientTicket.getEntries().remove(1);
        clientTicket.setTotalAmount(new BigDecimal("120"));

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_A
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=40000");
        this.jdbcTemplate.update("update BD_RISK_BETTING set total_amount=0 where ID='DIGITAL-2'");
        // only XD supported
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0 and PRIZE_LOGIC_ID='PL-DIG-111'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='13' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='12'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='23' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='22'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='33' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='32'");
        this.jdbcTemplate
                .update("update PRIZE_PARAMETERS set parameter_name='43' where PRIZE_LOGIC_ID='PL-DIG-111' and parameter_name='42'");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-999','PL-DIG-111', '51',50,2,1, '5th exactly matched 4D')");
        this.jdbcTemplate
                .update("insert into PRIZE_PARAMETERS(parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION) values('DIG-998','PL-DIG-111', '53',53,2,1, '5th mixed 4D')");
        this.jdbcTemplate.update("update PRIZE_PARAMETERS set bet_option=1 where PRIZE_LOGIC_ID='PL-DIG-111'");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OUT_OF_RISK_CONTROL, respCtx.getResponseCode());
    }

    /**
     * Verify the risk of the 4D entry. TypeA only supports 4D.
     */
    @Rollback(true)
    @Test
    public void testSell_FixLossRisk_TypeB_Cancel_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        // typeA only supports XD
        clientTicket.getEntries().remove(1);
        clientTicket.setTotalAmount(new BigDecimal("120"));

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_B
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=40000");
        this.jdbcTemplate.update("update BD_RISK_BETTING set total_amount=0 where ID='DIGITAL-2'");
        // only XD supported
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0");

        // 1. make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // 2. cancel by ticket
        DigitalTicket cancelTicket = new DigitalTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert risk control log
        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 41);
        assertEquals(50.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2", 31);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "3,2,8", 32);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3", 21);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "2,8", 22);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0", 11);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "8", 12);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);

        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 41);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2", 31);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "3,2,8", 32);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3", 21);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "2,8", 22);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0", 11);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "8", 12);
        assertEquals(0.0, log.getTotalAmount().doubleValue(), 0);
    }

    /**
     * Verify the risk of the 4D entry. TypeA only supports 4D.
     */
    @Rollback(true)
    @Test
    public void testSell_FixLossRisk_TypeB_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        // typeA only supports XD
        clientTicket.getEntries().remove(1);
        clientTicket.setTotalAmount(new BigDecimal("120"));

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_B
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=40000");
        this.jdbcTemplate.update("update BD_RISK_BETTING set total_amount=100 where ID='DIGITAL-1'");
        // only XD supported
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OUT_OF_RISK_CONTROL, respCtx.getResponseCode());

        // assert risk control log
        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 41);
        assertEquals(100.0, log.getTotalAmount().doubleValue(), 0);
    }

    @Test
    public void testSell_FixLossRisk_TypeC_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();

        this.jdbcTemplate.update("update PRIZE_LOGIC set ALGORITHM_ID='" + DigitalRiskControlService.ALGORITHM_TYPE_C
                + "'");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set CONTORL_METHOD=1");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set LOSS_AMOUNT=40000");
        this.jdbcTemplate.update("delete from PRIZE_PARAMETERS where bet_option<0");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert risk control log
        RiskControlLog log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-112", "0,3,2,8", 41);
        assertEquals(110.0, log.getTotalAmount().doubleValue(), 0);
        log = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber("GII-113", "0,3,2,8", 41);
        assertEquals(60.0, log.getTotalAmount().doubleValue(), 0);

        log = this.getBaseJpaDao().findById(RiskControlLog.class, "DIGITAL-2");
        assertEquals(240.0, log.getTotalAmount().doubleValue(), 0);
    }

    // ---------------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ---------------------------------------------------------------------------

    public RiskControlLogDao getRiskControlLogDao() {
        return riskControlLogDao;
    }

    public void setRiskControlLogDao(RiskControlLogDao riskControlLogDao) {
        this.riskControlLogDao = riskControlLogDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
