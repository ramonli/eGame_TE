package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;

import net.mpos.fk.util.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class TransferActivityReportDaoImpl extends BaseJpaDao implements ActivityReportDao {
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

        sql.append("select type, create_time, sum(total_amount), count(id)")
           .append("     from (select te.id,")
           .append("             d.transaction_type type,")
         //  .append("             te.virn,")
           .append("             NVL(te.total_amount, 0) as total_AMOUNT,")
           .append("             TO_CHAR(te.create_time, 'YYYYMMDD') AS create_time")
                   .append("        from balance_transactions d, te_transaction te")
                           .append(" where te.id = d.te_transaction_id")
                                   .append("         and te.type = 116")
                                           .append("            and te.response_code = 200")
                                                   .append("           and te.OPERATOR_ID = :operatorId")
                                                           .append("               and d.owner_id=:operatorId")
                                                                   .append("                  and d.status=:status")
        .append("           AND (te.create_time between to_date('" + strStartTime
                        + "', 'YYYYMMDDHH24MISS') and to_date('" + strEndTime + "', 'YYYYMMDDHH24MISS') ")
       .append(") )")
  .append(" group by type,  create_time");

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
            //select type, create_time, sum(total_amount), count(id)
            // assemble activity report.
            int transType = ((BigDecimal) columns[0]).intValue();

            String date = (String) columns[1];
            //String virn=(String)columns[1];
            

            BigDecimal amount = (BigDecimal) columns[2];
            int numberOfTrans = ((BigDecimal) columns[3]).intValue();

            
            int lastType=transType;
     

            DailyActivityReport dailyActivityReport = activityReport.getReportByDate(date, true);
            ActivityReportItem reportItem = dailyActivityReport.getReportItemByTransType(transType, true);
            reportItem.setTransType(lastType);
            reportItem.setAmount(amount);
            reportItem.setNumberOfTrans(reportItem.getNumberOfTrans() + numberOfTrans);
        }

        return activityReport.getDailyActivityReports();
    }

}
