package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.trans.domain.TransactionType;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class BaseActivityReportJpaDao extends BaseJpaDao implements ActivityReportDao {
    private String ticketTableName;
    private String columnMultiDrawName = "MUTLI_DRAW";

    @Override
    public List<DailyActivityReport> findActivityReport(String operatorId, Date startTime, Date endTime) {
        String strStartTime = new SimpleDateFormat("yyyyMMddHHmmss").format(startTime);
        String strEndTime = new SimpleDateFormat("yyyyMMddHHmmss").format(endTime);

        String sql = "SELECT a.update_date," + "  a.trans_type," + "  a.status,"
                + "  SUM(a.total_amount) AS total_amount," + "  COUNT(a.serial_no)  AS sale_count" + " FROM ("
                /**
                 * When a ticket is paid, its update time will be updated to payout time as well. If a ticket is sold
                 * yesterday, and claim payout today, it is definitely the sale should be counted to yesterday, so we
                 * must filter this sale by its create time here, otherwise it will be counted to today.
                 * <p>
                 * Use create_time as filter criteria
                 */
                + "  (SELECT TO_CHAR(t.CREATE_TIME,'YYYYMMDD') AS update_date," + "  t.TOTAL_AMOUNT*t."
                + this.getColumnMultiDrawName()
                + " AS total_amount,"
                + "    t.TRANS_TYPE AS trans_type,"
                + "    t.SERIAL_NO AS serial_no,"
                + "    t.STATUS AS status"
                + "  FROM "
                + this.getTicketTableName()
                + " t"
                + "  WHERE t.OPERATOR_ID=:operatorId"
                + "  AND (t.CREATE_TIME BETWEEN to_date('"
                + strStartTime
                + "','YYYYMMDDHH24MISS') AND to_date('"
                + strEndTime
                + "','YYYYMMDDHH24MISS'))"
                + "  AND t.TRANS_TYPE =200"
                + "  AND t.TICKET_TYPE=:ticketType"
                + "  AND t."
                + this.getColumnMultiDrawName()
                + " >0"
                + "  )"
                + "UNION ALL"
                /**
                 * Statistics of cancellation, use update_time as filter criteria.
                 */
                + "  (SELECT TO_CHAR(t.UPDATE_TIME,'YYYYMMDD') AS update_date,"
                + "    t.TOTAL_AMOUNT*t."
                + this.getColumnMultiDrawName()
                + " AS total_amount,"
                + "    t.TRANS_TYPE AS trans_type,"
                + "    t.SERIAL_NO AS serial_no,"
                + "    t.STATUS AS status"
                + "  FROM "
                + this.getTicketTableName()
                + " t"
                + "  WHERE t.OPERATOR_ID=:operatorId"
                + "  AND (t.UPDATE_TIME BETWEEN to_date('"
                + strStartTime
                + "','YYYYMMDDHH24MISS') AND to_date('"
                + strEndTime
                + "','YYYYMMDDHH24MISS'))"
                + "  AND t.TRANS_TYPE IN (201, 206, 210)"
                + "  AND t.TICKET_TYPE =:ticketType"
                + "  AND t."
                + this.getColumnMultiDrawName()
                + " >0"
                + "  ) ) a "
                + "GROUP BY a.UPDATE_DATE,"
                + "  a.TRANS_TYPE,"
                + "  a.STATUS";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorId", operatorId);
        params.put("ticketType", BaseTicket.TICKET_TYPE_NORMAL);

        return this.assembleGameActivityReport(sql, params);
    }

    /**
     * Assemble a list of <code>DailyReportItem</code> from the provided <code>sql</code>, and the returned result of
     * <code>sql</code> must be below columns and in the same order:
     * <p>
     * 
     * <pre>
     * SELECT a.update_date,
     *   a.trans_type,
     *   a.status,
     *   SUM(a.total_amount),
     *   count(a.serial_no)  
     * FROM
     *   (SELECT TO_CHAR(t.update_time,'YYYYMMDD') AS update_date,
     *     t.TOTAL_AMOUNT*t.MUTLI_DRAW             AS total_amount,
     *     t.TRANS_TYPE                            AS trans_type,
     *     t.SERIAL_NO as serial_no,
     *     t.STATUS AS status
     *   FROM TE_TICKET t
     *   WHERE t.OPERATOR_ID='OPERATOR-111'
     *   AND t.UPDATE_TIME BETWEEN to_date('20131212000000','YYYYMMDDHH24MISS') AND to_date('20131214000000','YYYYMMDDHH24MISS')
     *   AND t.TICKET_TYPE=1
     *   AND t.MUTLI_DRAW >0
     *   ) a
     * GROUP BY a.update_time,
     *   a.trans_type,
     *   a.status
     * </pre>
     * 
     * !! Above SQL has been proven that is incorrect, forget it.
     * 
     * @param sql
     *            The sql to query daily summary of a given game.
     * @param params
     *            the parameter for sql.
     * @return the list of daily summary report.
     */
    @SuppressWarnings("deprecation")
    protected List<DailyActivityReport> assembleGameActivityReport(final String sql, final Map<String, Object> params) {
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
            DailyActivityReport dailyReport = activityReport.getReportByDate(date, true);
            int transType = ((BigDecimal) columns[1]).intValue();
            int status = ((BigDecimal) columns[2]).intValue();
            /**
             * If a multi-draw ticket has been paid, and a new ticket is generated, the status of new generated ticket
             * will be 1(accepted) and trans type will be 'payout', this ticket record must be ignored, otherwise will
             * be counted into payout statistics.
             */
            if (transType == TransactionType.PAYOUT.getRequestType()) {
                continue;
            }
            if (status == BaseTicket.STATUS_CANCEL_DECLINED) {
                // for cancellation, there may be two rows with
                // same trans_type 201, however the one is
                // 'canceled' and other is cancel declined,
                // these 2 rows must be recorded in 2 activity
                // report item.
                transType = TransactionType.CANCEL_DECLINED.getRequestType();
            }
            /*
             * If a multi-draw ticket has been paid, the status of payout-started draw will be 5(paid), however the
             * status of tickets associating with active game instances will be 0(invalid)
             */
            ActivityReportItem reportItem = dailyReport.getReportItemByTransType(transType, true);
            reportItem.setAmount(reportItem.getAmount().add((BigDecimal) columns[3]));
            reportItem.setNumberOfTrans(reportItem.getNumberOfTrans() + ((BigDecimal) columns[4]).intValue());
        }

        return activityReport.getDailyActivityReports();
    }

    public String getTicketTableName() {
        return ticketTableName;
    }

    public void setTicketTableName(String ticketTableName) {
        this.ticketTableName = ticketTableName;
    }

    public String getColumnMultiDrawName() {
        return columnMultiDrawName;
    }

    public void setColumnMultiDrawName(String columnMultiDrawName) {
        this.columnMultiDrawName = columnMultiDrawName;
    }

}
