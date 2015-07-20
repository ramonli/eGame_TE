package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class ActivityReportDaoImpl extends BaseJpaDao implements ActivityReportDao {
    private Log logger = LogFactory.getLog(ActivityReportDaoImpl.class);

    /**
     * Only statistics cashout,topop,sale,payout.
     */
    @Override
    public List<DailyActivityReport> findActivityReport(String operatorId, Date startTime, Date endTime) {
        String strStartTime = new SimpleDateFormat("yyyyMMddHHmmss").format(startTime);
        String strEndTime = new SimpleDateFormat("yyyyMMddHHmmss").format(endTime);
        final StringBuffer sql = new StringBuffer();
        sql.append(" select a.create_time,a.TRANSACTION_TYPE,SUM(NVL(a.COMMISION_AMOUNT,0)) ")
                .append("  from (SELECT TO_CHAR(t.create_time, 'YYYYMMDD') AS create_time,t.COMMISION_AMOUNT, ")
                .append("               CASE ")
                .append("                  WHEN t.OPERATOR_ID = t.OWNER_ID THEN ")
                .append("                  t.TRANSACTION_TYPE ")
                .append("               ELSE ")
                .append("                 to_number('" + Constants.DESTINATION_TRANSTYPE_PREFIX
                        + "'||to_char(t.TRANSACTION_TYPE)) ")
                .append("               END TRANSACTION_TYPE ")
                .append("          FROM balance_transactions t ")
                .append("         WHERE  t.OWNER_ID = :operatorId ")
                .append("           and t.STATUS = :status ")
                // .append("           and t.TRANSACTION_TYPE in (352, 353, 445, 365, 447, 360) ")
                .append("           AND (t.create_time between ")
                .append("               to_date('" + strStartTime + "','YYYYMMDDHH24MISS') and ")
                .append("               to_date('" + strEndTime + "','YYYYMMDDHH24MISS'))) a ")
                .append("  GROUP BY a.create_time,a.TRANSACTION_TYPE ");

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
            String date = (String) columns[0];
            int transType = ((BigDecimal) columns[1]).intValue();
            BigDecimal commissionAmount = (BigDecimal) columns[2];
            if (transType == TransactionType.CANCEL_BY_TRANSACTION.getRequestType()
                    || transType == TransactionType.CANCEL_BY_TICKET.getRequestType()
                    || transType == TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType()) {
                continue;
            }

            DailyActivityReport dailyActivityReport = activityReport.getReportByDate(date, true);
            ActivityReportItem reportItem = dailyActivityReport.getReportItemByTransType(transType, true);
            reportItem.setTransType(transType);
            // 因为sale,payout已经在sale,payout逻辑中已经统计了amount,numberOfTrans,所以这里只统计income_balance_transfer,topup
            // if (transType == TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType()
            // || transType == TransactionType.INCOME_BALANCE_TRANSFER.getRequestType())
            // {
            // reportItem.setAmount(amount);
            // reportItem.setNumberOfTrans(reportItem.getNumberOfTrans()+numberOfTrans);
            // }
            commissionAmount = commissionAmount == null ? new BigDecimal("0") : commissionAmount;
            reportItem.setCommission(commissionAmount);
        }

        return activityReport.getDailyActivityReports();
    }

}
