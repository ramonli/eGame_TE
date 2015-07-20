package com.mpos.lottery.te.common.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A callback implementation to support JDBC batch operation, refer to {@link BaseJpaDao#batch(BatchCallback)}
 * <p/>
 * 
 * Below is a sample, where <code>Transaction</code> is a POJO.
 * 
 * <pre>
 * &#064;Override
 * public void updateSettlementFlagByTransaction(List&lt;Transaction&gt; transactions) {
 *     if (transactions == null)
 *         return;
 *     this.batch(new BatchCallback&lt;Transaction&gt;(transactions) {
 * 
 *         &#064;Override
 *         public String getQuery() {
 *             return &quot;update eig_te_ticket set SETTLEMENT_FLAG=?,UPDATE_TIME=?,SETTLEMENT_TIME=? &quot;
 *                     + &quot;where TRANSACTION_ID=?&quot;;
 *         }
 * 
 *         &#064;Override
 *         public void setBatchParameter(PreparedStatement ps, Transaction trans) throws SQLException {
 *             Timestamp now = new Timestamp(new Date().getTime());
 *             ps.setInt(1, trans.getSettlementFlag());
 *             ps.setTimestamp(2, now);
 *             ps.setTimestamp(3, now);
 *             ps.setString(4, trans.getId());
 *         }
 *     });
 * }
 * </pre>
 * 
 * @author Ramon Li
 */
public abstract class BatchCallback<T> {
    private List<T> parameters;

    public BatchCallback(List<T> parameters) {
        this.parameters = parameters;
    }

    public abstract String getQuery();

    public void assembleParameters(PreparedStatement ps) throws SQLException {
        for (T t : this.parameters) {
            this.setBatchParameter(ps, t);
            ps.addBatch();
        }
    }

    public abstract void setBatchParameter(PreparedStatement ps, T t) throws SQLException;
}
