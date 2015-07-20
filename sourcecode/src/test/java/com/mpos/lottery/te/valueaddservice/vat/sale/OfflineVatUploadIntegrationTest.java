package com.mpos.lottery.te.valueaddservice.vat.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.OfflineTicketLog;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.gamespec.sale.dao.OfflineTicketLogDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.VatDomainMocker;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;
import com.mpos.lottery.te.valueaddservice.vat.web.VatOfflineSaleUploadDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatSaleTransactionDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class OfflineVatUploadIntegrationTest extends BaseServletIntegrationTest {
    private static Log logger = LogFactory.getLog(OfflineVatUploadIntegrationTest.class);
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;
    @Resource(name = "vatOperatorBalanceDao")
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @Resource(name = "offlineTicketLogDao")
    private OfflineTicketLogDao offlineTicketLogDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;

    @Rollback(true)
    @Test
    public void testUpload_AllSuccess() throws Exception {
        this.printMethod();
        VatOfflineSaleUploadDto uploadDto = VatDomainMocker.mockOfflineVatUploadDto();

        BigDecimal oldVatBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111").getSaleBalance();
        BigDecimal oldSaleBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getSaleCreditLevel();

        // make sale
        Context uploadReqCtx = this.getDefaultContext(TransactionType.VAT_UPLOAD_OFFLINESALE.getRequestType(),
                uploadDto);
        Context uploadRespCtx = doPost(this.mockRequest(uploadReqCtx));
        VatOfflineSaleUploadDto respUploadDto = (VatOfflineSaleUploadDto) uploadRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, uploadRespCtx.getResponseCode());
        assertEquals(0, respUploadDto.getVatSaleList().size());
        assertEquals(uploadDto.getVatSaleList().size(), respUploadDto.getCount());
        assertEquals(0, respUploadDto.getCountOfFailure());
        assertEquals(uploadDto.getVatSaleList().size(), respUploadDto.getCountOfSuccess());

        // assert vat balance of operator
        assertEquals(oldVatBalance.add(uploadDto.calVatTotalAmount()).doubleValue(), this.getVatOperatorBalanceDao()
                .findByOperator("OPERATOR-111").getSaleBalance().doubleValue(), 0);
        BigDecimal totalTicketAmount = new BigDecimal("0");
        for (VatSaleTransactionDto t : uploadDto.getVatSaleList()) {
            if (t.getTicketDto() != null) {
                totalTicketAmount = totalTicketAmount.add(t.getTicketDto().getTotalAmount());
            }
        }
        // assert sale balance operator
        assertEquals(oldSaleBalance.subtract(totalTicketAmount).doubleValue(),
                this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getSaleCreditLevel().doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, uploadRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(uploadRespCtx.getTransactionID());
        // expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(uploadDto.calVatTotalAmount());
        // expectTrans.setTicketSerialNo(respUploadDto.getSerialNo());
        expectTrans.setOperatorId(uploadRespCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(uploadRespCtx.getTerminalId());
        expectTrans.setTraceMessageId(uploadRespCtx.getTraceMessageId());
        expectTrans.setType(uploadReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        List<OfflineTicketLog> logs = this.getOfflineTicketLogDao().findByTransaction(uploadRespCtx.getTransactionID());
        assertEquals(3, logs.size());

        // assert vat sale transaction
        for (VatSaleTransactionDto clientVatTrans : uploadDto.getVatSaleList()) {
            VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByRefNo(clientVatTrans.getVatRefNo());
            assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
            assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
            assertEquals("COMPANY-1", hostVatTrans.getSellerCompanyId());
            assertEquals("COMPANY-2", hostVatTrans.getBuyerCompanyId());
            assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount()
                    .doubleValue(), 0);
            assertEquals(clientVatTrans.getVatTotalAmount().multiply(clientVatTrans.getVatRate()).doubleValue(),
                    hostVatTrans.getVatRateTotalAmount().doubleValue(), 0);
            if (clientVatTrans.getTicketDto() != null) {
                assertEquals(clientVatTrans.getTicketDto().getSerialNo(), hostVatTrans.getTicketSerialNo());
                // assertEquals("GII-111", hostVatTrans.getGameInstanceId());
                assertNotNull(hostVatTrans.getGameInstanceId());
                assertEquals(clientVatTrans.getTicketDto().getTotalAmount().doubleValue(), hostVatTrans
                        .getSaleTotalAmount().doubleValue(), 0);
            }
            // assertEquals(GameType.LUCKYNUMBER.getType(),
            // hostVatTrans.getGameType());
            assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
            assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
            assertEquals(OperatorBizType.BIZ_B2B, hostVatTrans.getBusinessType());

            if (clientVatTrans.getTicketDto() != null) {
                // assert offline tickets
                OfflineTicketLog log = this.lookup(logs, clientVatTrans.getTicketDto().getSerialNo());
                assertNotNull(log);

                if (GameType.RAFFLE.getType() == hostVatTrans.getGameType()) {
                    // assert Raffle tickets
                    List<RaffleTicket> rTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(1, rTickets.size());
                    assertEquals(BaseTicket.STATUS_ACCEPTED, rTickets.get(0).getStatus());
                    assertEquals(log.getGameInstanceId(), rTickets.get(0).getGameInstance().getId());
                } else if (GameType.LUCKYNUMBER.getType() == hostVatTrans.getGameType()) {
                    // assert magic100 tickets
                    List<Magic100Ticket> mTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(1, mTickets.size());
                    assertEquals(BaseTicket.STATUS_ACCEPTED, mTickets.get(0).getStatus());
                    assertEquals(log.getGameInstanceId(), mTickets.get(0).getGameInstance().getId());

                    // assert matic100 entries
                    List<Magic100Entry> hostEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(2, hostEntries.size());
                    for (Magic100Entry entry : hostEntries) {
                        assertEquals(1, entry.getTotalBets());
                        assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
                    }
                }
            }
        }
    }

    @Test
    public void testUpload_PartialSuccess() throws Exception {
        this.printMethod();
        VatOfflineSaleUploadDto uploadDto = VatDomainMocker.mockOfflineVatUploadDto();
        // trigger a failed VAT transaction
        uploadDto.getVatSaleList().get(1).setVatCode("NO-Exist");
        VatSaleTransactionDto failedDto = uploadDto.getVatSaleList().get(1);

        BigDecimal oldVatBalance = this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111").getSaleBalance();
        BigDecimal oldSaleBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getSaleCreditLevel();

        // make sale
        Context uploadReqCtx = this.getDefaultContext(TransactionType.VAT_UPLOAD_OFFLINESALE.getRequestType(),
                uploadDto);
        Context uploadRespCtx = doPost(this.mockRequest(uploadReqCtx));
        VatOfflineSaleUploadDto respUploadDto = (VatOfflineSaleUploadDto) uploadRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, uploadRespCtx.getResponseCode());
        assertEquals(1, respUploadDto.getVatSaleList().size());
        assertEquals(failedDto.getVatRefNo(), respUploadDto.getVatSaleList().get(0).getVatRefNo());
        assertEquals(SystemException.CODE_VAT_NOFOUND, respUploadDto.getVatSaleList().get(0).getStatusCode());
        assertEquals(uploadDto.getVatSaleList().size(), respUploadDto.getCount());
        assertEquals(1, respUploadDto.getCountOfFailure());
        assertEquals(uploadDto.getVatSaleList().size() - 1, respUploadDto.getCountOfSuccess());

        // assert vat balance of operator
        assertEquals(oldVatBalance.add(uploadDto.calVatTotalAmount().subtract(failedDto.getVatTotalAmount()))
                .doubleValue(), this.getVatOperatorBalanceDao().findByOperator("OPERATOR-111").getSaleBalance()
                .doubleValue(), 0);
        BigDecimal totalTicketAmount = new BigDecimal("0");
        for (VatSaleTransactionDto t : uploadDto.getVatSaleList()) {
            if (t.getTicketDto() != null) {
                totalTicketAmount = totalTicketAmount.add(t.getTicketDto().getTotalAmount());
            }
        }
        // assert sale balance operator
        assertEquals(oldSaleBalance.subtract(totalTicketAmount.subtract(failedDto.getTicketDto().getTotalAmount()))
                .doubleValue(), this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getSaleCreditLevel()
                .doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, uploadRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(uploadRespCtx.getTransactionID());
        // expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(uploadDto.calVatTotalAmount().subtract(failedDto.getVatTotalAmount()));
        // expectTrans.setTicketSerialNo(respUploadDto.getSerialNo());
        expectTrans.setOperatorId(uploadRespCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(uploadRespCtx.getTerminalId());
        expectTrans.setTraceMessageId(uploadRespCtx.getTraceMessageId());
        expectTrans.setType(uploadReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        List<OfflineTicketLog> logs = this.getOfflineTicketLogDao().findByTransaction(uploadRespCtx.getTransactionID());
        assertEquals(2, logs.size());

        // assert vat sale transaction
        for (VatSaleTransactionDto clientVatTrans : uploadDto.getVatSaleList()) {
            VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByRefNo(clientVatTrans.getVatRefNo());
            if (clientVatTrans.getVatRefNo().equalsIgnoreCase(failedDto.getVatRefNo())) {
                // failed one, ignore it
                assertNull(hostVatTrans);
                continue;
            }
            assertEquals(dbTrans.getOperatorId(), hostVatTrans.getOperatorId());
            assertEquals(dbTrans.getMerchantId(), hostVatTrans.getMerchantId());
            assertEquals("COMPANY-1", hostVatTrans.getSellerCompanyId());
            assertEquals("COMPANY-2", hostVatTrans.getBuyerCompanyId());
            assertEquals(clientVatTrans.getVatTotalAmount().doubleValue(), hostVatTrans.getVatTotalAmount()
                    .doubleValue(), 0);
            assertEquals(clientVatTrans.getVatTotalAmount().multiply(clientVatTrans.getVatRate()).doubleValue(),
                    hostVatTrans.getVatRateTotalAmount().doubleValue(), 0);
            if (clientVatTrans.getTicketDto() != null) {
                assertEquals(clientVatTrans.getTicketDto().getSerialNo(), hostVatTrans.getTicketSerialNo());
                // assertEquals("GII-111", hostVatTrans.getGameInstanceId());
                assertNotNull(hostVatTrans.getGameInstanceId());
                assertEquals(clientVatTrans.getTicketDto().getTotalAmount().doubleValue(), hostVatTrans
                        .getSaleTotalAmount().doubleValue(), 0);
            }
            // assertEquals(GameType.LUCKYNUMBER.getType(),
            // hostVatTrans.getGameType());
            assertEquals(this.getVatDao().findByCode(clientVatTrans.getVatCode()).getId(), hostVatTrans.getVatId());
            assertEquals(VatSaleTransaction.STATUS_VALID, hostVatTrans.getStatus());
            assertEquals(OperatorBizType.BIZ_B2B, hostVatTrans.getBusinessType());

            if (clientVatTrans.getTicketDto() != null) {
                // assert offline tickets
                OfflineTicketLog log = this.lookup(logs, clientVatTrans.getTicketDto().getSerialNo());
                assertNotNull(log);

                if (GameType.RAFFLE.getType() == hostVatTrans.getGameType()) {
                    // assert Raffle tickets
                    List<RaffleTicket> rTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(1, rTickets.size());
                    assertEquals(BaseTicket.STATUS_ACCEPTED, rTickets.get(0).getStatus());
                    assertEquals(log.getGameInstanceId(), rTickets.get(0).getGameInstance().getId());
                    assertTrue(rTickets.get(0).isOffline());
                } else if (GameType.LUCKYNUMBER.getType() == hostVatTrans.getGameType()) {
                    // assert magic100 tickets
                    List<Magic100Ticket> mTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(1, mTickets.size());
                    assertEquals(BaseTicket.STATUS_ACCEPTED, mTickets.get(0).getStatus());
                    assertEquals(log.getGameInstanceId(), mTickets.get(0).getGameInstance().getId());
                    assertTrue(mTickets.get(0).isOffline());

                    // assert matic100 entries
                    List<Magic100Entry> hostEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                            clientVatTrans.getTicketDto().getSerialNo(), false);
                    assertEquals(2, hostEntries.size());
                    for (Magic100Entry entry : hostEntries) {
                        assertEquals(1, entry.getTotalBets());
                        assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------

    protected OfflineTicketLog lookup(List<OfflineTicketLog> logs, String serialNo) {
        for (OfflineTicketLog log : logs) {
            if (log.getSerialNo().equalsIgnoreCase(serialNo)) {
                return log;
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // ----------------------------------------------------------

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

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public OfflineTicketLogDao getOfflineTicketLogDao() {
        return offlineTicketLogDao;
    }

    public void setOfflineTicketLogDao(OfflineTicketLogDao offlineTicketLogDao) {
        this.offlineTicketLogDao = offlineTicketLogDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

}
