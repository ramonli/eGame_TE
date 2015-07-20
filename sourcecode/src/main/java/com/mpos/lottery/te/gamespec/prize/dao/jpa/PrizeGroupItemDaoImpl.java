package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeGroupItemDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PrizeGroupItemDaoImpl extends BaseJpaDao implements PrizeGroupItemDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<PrizeGroupItem> findByGroupAndGameTypeAndGroupType(final String prizeGroupId, final int gameType,
            final int groupType) {
        String sql = "SELECT I.ID,I.PRIZE_LEVEL FROM BD_PRIZE_GROUP_ITEM I "
                + "WHERE I.BD_PRIZE_GROUP_ID=? AND I.GAME_TYPE=? AND I.PRIZE_TYPE=?";

        return this.queryList(sql, new JdbcQueryCallback() {

            @Override
            public void setParameter(PreparedStatement ps) throws SQLException {
                ps.setString(1, prizeGroupId);
                ps.setInt(2, gameType);
                ps.setInt(3, groupType);
            }

            @Override
            public Object objectFromRow(ResultSet rs) throws SQLException {
                PrizeGroupItem item = new PrizeGroupItem();
                item.setId(rs.getString(1));
                item.setPrizeGroupId(prizeGroupId);
                item.setStrPrizeLevel(rs.getString(2));
                item.setGameType(gameType);
                item.setGroupType(groupType);
                return item;
            }

        });
    }
}
