package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.PrizeObjectDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.PrizeObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PrizeObjectDaoImpl extends BaseJpaDao implements PrizeObjectDao {

    @Override
    public PrizeObject findByPrizeLogicAndLevel(final String prizeLogicId, final int prizeLevel, final int winningType,
            final int version) throws SQLException {
        String sql = "select o.id,o.object_name,o.price,o.tax_amount from "
                + "OBJECT_PRIZE_PARAMETERS p, BD_PRIZE_OBJECT o where p.PRIZE_LOGIC_ID=? "
                + "and p.PRIZE_LEVEL=? and p.BD_PRIZE_OBJECT_ID=o.ID";
        List result = this.queryList(sql, new JdbcQueryCallback() {

            @Override
            public void setParameter(PreparedStatement ps) throws SQLException {
                ps.setString(1, prizeLogicId);
                ps.setInt(2, prizeLevel);
            }

            @Override
            public Object objectFromRow(ResultSet rs) throws SQLException {
                PrizeObject object = new PrizeObject();
                object.setId(rs.getString(1));
                object.setName(rs.getString(2));
                object.setPrizeAmount(rs.getBigDecimal(3));
                object.setTax(rs.getBigDecimal(4));
                return object;
            }

        });

        if (result.size() == 0) {
            return null;
        } else if (result.size() == 1) {
            return (PrizeObject) result.get(0);
        } else {
            throw new SQLException("total " + result.size() + " prize objects found by(prizeLogicId=" + prizeLogicId
                    + ",prizeLevel=" + prizeLevel + "), should only 1 exist!");
        }
    }

}
