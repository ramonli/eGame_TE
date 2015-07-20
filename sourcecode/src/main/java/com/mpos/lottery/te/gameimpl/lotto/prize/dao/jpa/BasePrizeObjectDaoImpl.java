/**
 * 
 */
package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.BasePrizeObjectDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 
 * @author Ramon Li
 */
public class BasePrizeObjectDaoImpl extends BaseJpaDao implements BasePrizeObjectDao {

    /*
     * (non-Javadoc)
     * 
     * @see com.mpos.lottery.te.gameimpl.lotto.prize.dao.BasePrizeObjectDao# findByParentObjectId (java.lang.String)
     */
    @Override
    public int findByParentObjectId(final String parentObjectId) {
        List result = this.queryList("select i.object_num from bd_prize_object p, "
                + "bd_prize_object_item i where p.id = i.bd_prize_object_item_id " + "and i.bd_prize_object_id=?",
                new JdbcQueryCallback() {

                    @Override
                    public void setParameter(PreparedStatement ps) throws SQLException {
                        ps.setString(1, parentObjectId);
                    }

                    @Override
                    public Object objectFromRow(ResultSet rs) throws SQLException {
                        return new Integer(rs.getInt(1));
                    }
                });
        if (result.size() == 0) {
            throw new SystemException("can't find child prize object by parent prize object(id=" + parentObjectId
                    + ").");
        }
        return ((Integer) result.get(0)).intValue();
    }
}
