package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class PayoutDaoImpl extends BaseJpaDao implements PayoutDao, ActivityReportDao {
    @Override
    public List<Payout> getByTicketSerialNo(String ticketSerialNo) throws DataAccessException {
        Map params = new HashMap(0);
        params.put("ticketSerialNo", ticketSerialNo);
        return this.findByNamedParams("from Payout p where p.ticketSerialNo=:ticketSerialNo", params);
    }

    @Override
    public List<Payout> getByTicketSerialNoAndStatus(String ticketSerialNo, int status) throws DataAccessException {
        Map params = new HashMap(0);
        params.put("ticketSerialNo", ticketSerialNo);
        params.put("status", status);
        return this.findByNamedParams("from Payout p where "
                + "p.ticketSerialNo=:ticketSerialNo and p.status=:status order by p.id", params);
    }

    @Override
    public List<Payout> getByTransactionAndStatus(String transactionId, int status) throws DataAccessException {
        Transaction trans = this.getEntityManager().getReference(Transaction.class, transactionId);
        Map params = new HashMap(0);
        params.put("transaction", trans);
        params.put("status", status);
        return this.findByNamedParams("from Payout p where p.transaction=:transaction and p.status=:status", params);
    }

    @Override
    public List<Payout> getByTransactionAndTicketAndStatus(String transactionId, String ticketSerialNo, int status)
            throws DataAccessException {
        Transaction trans = this.getEntityManager().getReference(Transaction.class, transactionId);
        Map params = new HashMap(0);
        params.put("transaction", trans);
        params.put("ticketSerialNo", ticketSerialNo);
        params.put("status", status);
        return this.findByNamedParams("from Payout p where  p.transaction=:transaction "
                + "and p.ticketSerialNo=:ticketSerialNo and p.status=:status", params);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<DailyActivityReport> findActivityReport(String operatorId, Date startTime, Date endTime) {
        String strStartTime = new SimpleDateFormat("yyyyMMddHHmmss").format(startTime);
        String strEndTime = new SimpleDateFormat("yyyyMMddHHmmss").format(endTime);

        final String sql = "select a.UPDATE_DATE,COUNT(DISTINCT(a.TICKET_SERIALNO)),SUM(a.PRIZE_AMOUNT),"
                + "SUM(a.TAX_AMOUNT) from (SELECT TO_CHAR(p.UPDATE_TIME,'YYYYMMDD') AS UPDATE_DATE, "
                + "p.TOTAL_AMOUNT_B4_TAX as PRIZE_AMOUNT, (p.TOTAL_AMOUNT_B4_TAX-p.TOTAL_AMOUNT) as TAX_AMOUNT,"
                + "p.TICKET_SERIALNO FROM PAYOUT p  WHERE p.OPERATOR_ID=:operatorId AND p.STATUS=:status "
                + "AND (p.UPDATE_TIME between to_date('" + strStartTime + "','YYYYMMDDHH24MISS') and to_date('"
                + strEndTime + "','YYYYMMDDHH24MISS'))) a GROUP BY a.UPDATE_DATE";

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorId", operatorId);
        params.put("status", Payout.STATUS_PAID);

        // execute query
        Query query = this.getEntityManager().createNativeQuery(sql);
        // set parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List rows = query.getResultList();
        ActivityReport activityReport = new ActivityReport();
        for (Object row : rows) {
            Object[] columns = (Object[]) row;
            // assemble activity report.
            String date = (String) columns[0];
            DailyActivityReport gameReport = activityReport.getReportByDate(date, true);
            // must handle 'return' and 'payout'
            ActivityReportItem reportItem = gameReport.getReportItemByTransType(
                    TransactionType.PAYOUT.getRequestType(), true);
            reportItem.setAmount((BigDecimal) columns[2]);
            reportItem.setTax((BigDecimal) columns[3]);
            reportItem.setNumberOfTrans(((BigDecimal) columns[1]).intValue());
        }

        return activityReport.getDailyActivityReports();
    }
}
