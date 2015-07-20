package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class GeneralActivityReportDaoImpl extends BaseJpaDao implements ActivityReportDao {
    private Log logger = LogFactory.getLog(GeneralActivityReportDaoImpl.class);

    /**
     * Only statistics cashout,topop,sale,payout
     * 
     * @param operatorId
     * @param startTime
     * @param endTime
     * @return list
     */
    @Override
    public List<DailyActivityReport> findActivityReport(String operatorId, Date startTime, Date endTime) {
        String strStartTime = new SimpleDateFormat("yyyyMMddHHmmss").format(startTime);
        String strEndTime = new SimpleDateFormat("yyyyMMddHHmmss").format(endTime);
        StringBuffer sql = new StringBuffer();
        sql.append(" select a.TRANSACTION_TYPE, ")
                .append("       a.create_time, ")
                .append("       SUM(a.total_AMOUNT), ")
                .append("       count(a.id) ")
                .append("  from (SELECT TO_CHAR(t.create_time, 'YYYYMMDD') AS create_time, ")
                .append("               NVL(t.BALANCE_AMOUNT,0) as total_AMOUNT, ")
                .append("               t.id, ")
                .append("               t.TRANSACTION_TYPE ")
                .append("          FROM balance_transactions t ")
                .append("         WHERE t.OPERATOR_ID = :operatorId ")
                .append("           and t.OWNER_ID = :operatorId ")
                .append("           and t.STATUS= :status ")
                // Income Balance Transfer(350),Operator Cash Out By Pass(352),Operator Cash Out
                // Manually(363),Cashout of customer(445)
                // cashout withdraw(365),? Top up by voucher(447),portal top up(360),Airtime Topup(455)
                .append("           and t.TRANSACTION_TYPE in (352,353,445,365,350,447,360,455,455,456) ")
                .append("           AND (t.create_time between to_date('" + strStartTime
                        + "', 'YYYYMMDDHH24MISS') and to_date('" + strEndTime + "', 'YYYYMMDDHH24MISS')) ")
                .append("           ) a ").append("  GROUP BY a.TRANSACTION_TYPE,a.create_time ");

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorId", operatorId);
        params.put("status", BalanceTransactions.STATUS_VALID);
        // execute query
        Query query = this.getEntityManager().createNativeQuery(sql.toString());
        // set parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List rows = query.getResultList();
        ActivityReport activityReport = new ActivityReport();
        for (Object row : rows) {
            Object[] columns = (Object[]) row;
            // assemble activity report.
            int transType = ((BigDecimal) columns[0]).intValue();
            String date = (String) columns[1];
            BigDecimal amount = (BigDecimal) columns[2];
            int numberOfTrans = ((BigDecimal) columns[3]).intValue();

            DailyActivityReport dailyActivityReport = activityReport.getReportByDate(date, true);
            ActivityReportItem reportItem = dailyActivityReport.getReportItemByTransType(transType, true);
            reportItem.setTransType(transType);
            reportItem.setAmount(amount);
            reportItem.setNumberOfTrans(reportItem.getNumberOfTrans() + numberOfTrans);
        }

        return activityReport.getDailyActivityReports();
    }

}
