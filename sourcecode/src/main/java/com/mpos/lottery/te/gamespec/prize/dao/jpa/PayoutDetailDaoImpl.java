package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * payout detail dao implements.
 * 
 */
public class PayoutDetailDaoImpl extends BaseJpaDao implements PayoutDetailDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<PayoutDetail> findByTransactions(List<String> transactionIds) {
        if (transactionIds.size() == 0) {
            return new ArrayList<PayoutDetail>();
        }

        StringBuffer transIdBuffer = new StringBuffer();
        for (int i = 0; i < transactionIds.size(); i++) {
            transIdBuffer.append("'").append(transactionIds.get(i)).append("'");
            if (i != (transactionIds.size() - 1)) {
                transIdBuffer.append(",");
            }
        }
        String sql = "select d.BG_LUCKY_PRIZE_OBJECT_ID,d.BG_LUCKY_PRIZE_OBJECT_NAME,d.OBJECT_NUM,"
                + "p.TRANSACTION_ID from PAYOUT p, PAYOUT_DETAIL d where P.ID=d.PAYOUT_ID and d.PAYOUT_TYPE="
                + PrizeLevelDto.PRIZE_TYPE_OBJECT + " and p.TRANSACTION_ID in (" + transIdBuffer.toString() + ")";
        return (List<PayoutDetail>) this.queryList(sql, new JdbcQueryCallback() {

            @Override
            public void setParameter(PreparedStatement ps) throws SQLException {
                // do nothing
            }

            @Override
            public Object objectFromRow(ResultSet rs) throws SQLException {
                PayoutDetail pd = new PayoutDetail();
                pd.setObjectId(rs.getString(1));
                pd.setObjectName(rs.getString(2));
                pd.setNumberOfObject(rs.getInt(3));
                pd.setTransactionId(rs.getString(4));
                return pd;
            }
        });
    }

    @Override
    public List<PayoutDetail> findByPayout(String payoutId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payoutId", payoutId);
        return this.findByNamedParams("from PayoutDetail d where d.payoutId=:payoutId", params);
    }
}
