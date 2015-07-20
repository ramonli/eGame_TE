package com.mpos.lottery.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalWinningItem;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalTicket;
import com.mpos.lottery.te.gameimpl.digital.sale.dao.DigitalEntryDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.LuckyWinningItemDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningItem;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gameimpl.lotto.sale.service.impl.LottoEntryDao;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningItemDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.junit.Test;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.util.List;

import javax.annotation.Resource;

@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class ConvertPlainTicketsToEncrypted extends BaseTransactionalIntegrationTest {
    @Resource(name = "payoutDetailDao")
    PayoutDetailDao payoutDetailDao;

    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;

    @Resource(name = "jpaLottoEntryDao")
    private LottoEntryDao lottoEntryDao;

    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;

    @Resource(name = "luckyWinningItemDao")
    private LuckyWinningItemDao luckyWinningItemDao;

    @Resource(name = "baseWinningItemDao")
    private BaseWinningItemDao baseWinningItemDao;

    @Resource(name = "digitalEntryDao")
    DigitalEntryDao digitalEntryDao;

    @Test
    public void testConvertLottoPlainTicketsToEncrypted() {

        // LottoTicket lottoTickt=null;
        LottoEntry lottoEntry = null;
        // get all tickets from db
        List<LottoTicket> list = this.getBaseJpaDao().all(LottoTicket.class);

        if (list != null) {
            for (LottoTicket lottoTicket : list) {
                // 1,get encrypted ticket serial number and update tickets
                String oldSerial = lottoTicket.getSerialNo();
                if (oldSerial != null && oldSerial.length() > 20) {
                    continue;
                }
                String newSerial = BaseTicket.encryptSerialNo(oldSerial);
                lottoTicket.setSerialNo(newSerial);
                this.getBaseJpaDao().update(lottoTicket);
                // 2,update entries
                List<LottoEntry> entryList = this.lottoEntryDao.findBySerialNo(oldSerial);
                if (entryList != null) {
                    for (LottoEntry entry : entryList) {
                        entry.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(entry);
                    }
                }

                // 3,update transactions
                List<Transaction> transactionList = this.transactionDao.getAllByTicketSerial(oldSerial);
                if (transactionList != null) {
                    for (Transaction transaction : transactionList) {
                        transaction.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(transaction);
                    }
                }
                // 4,update payout
                List<Payout> payoutList = this.getPayoutDao().getByTicketSerialNo(oldSerial);
                if (payoutList != null) {
                    for (Payout payout : payoutList) {
                        payout.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(payout);
                    }
                }
                // 5,wining ticket,dailry cash winning,object winning ,winng
                List<WinningItem> winningList = this.getBaseWinningItemDao().findAllBySerialNo(WinningItem.class,
                        oldSerial);

                if (winningList != null) {
                    for (WinningItem winning : winningList) {
                        winning.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(winning);
                    }
                }
                // 2,get
            }// end for( LottoTicket lottoTickt:list){
        }

        logger.info("Converted all plain Lotto tickets  to encrypted ticket successfully!");

    }

    @Test
    public void testConvertDigitalPlainTicketsToEncrypted() {

        // LottoTicket lottoTickt=null;
        LottoEntry lottoEntry = null;
        // get all tickets from db
        List<DigitalTicket> list = this.getBaseJpaDao().all(DigitalTicket.class);

        if (list != null) {
            for (DigitalTicket digitalTicket : list) {
                // 1,get encrypted ticket serial number and update tickets
                String oldSerial = digitalTicket.getSerialNo();

                String newSerial = BaseTicket.encryptSerialNo(oldSerial);
                if (oldSerial != null && oldSerial.length() > 20) {
                    continue;
                }
                digitalTicket.setSerialNo(newSerial);
                this.getBaseJpaDao().update(digitalTicket);
                // 2,update entries
                List<DigitalEntry> entryList = this.digitalEntryDao.findByTicketSerialNo(oldSerial);
                if (entryList != null) {
                    for (DigitalEntry entry : entryList) {
                        entry.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(entry);
                    }
                }

                // 3,update transactions
                List<Transaction> transactionList = this.transactionDao.getAllByTicketSerial(oldSerial);
                if (transactionList != null) {
                    for (Transaction transaction : transactionList) {
                        transaction.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(transaction);
                    }
                }
                // 4,update payout
                List<Payout> payoutList = this.getPayoutDao().getByTicketSerialNo(oldSerial);
                if (payoutList != null) {
                    for (Payout payout : payoutList) {
                        payout.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(payout);
                    }
                }
                // 5,wining ticket,dailry cash winning,object winning ,winng
                List<DigitalWinningItem> winningList = this.getBaseWinningItemDao().findAllBySerialNo(
                        DigitalWinningItem.class, oldSerial);

                if (winningList != null) {
                    for (DigitalWinningItem winning : winningList) {
                        winning.setTicketSerialNo(newSerial);
                        this.getBaseJpaDao().update(winning);
                    }
                }
                // 2,get
            }// end for( LottoTicket lottoTickt:list){
        }

        // System.out.println(this.jdbcTemplate.queryFor)Int("select 1 from dual"));
        logger.info("Converted all plain Digital tickets  to encrypted ticket successfully!");

    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public LuckyWinningItemDao getLuckyWinningItemDao() {
        return luckyWinningItemDao;
    }

    public void setLuckyWinningItemDao(LuckyWinningItemDao luckyWinningItemDao) {
        this.luckyWinningItemDao = luckyWinningItemDao;
    }

    public BaseWinningItemDao getBaseWinningItemDao() {
        return baseWinningItemDao;
    }

    public void setBaseWinningItemDao(BaseWinningItemDao baseWinningItemDao) {
        this.baseWinningItemDao = baseWinningItemDao;
    }

}
