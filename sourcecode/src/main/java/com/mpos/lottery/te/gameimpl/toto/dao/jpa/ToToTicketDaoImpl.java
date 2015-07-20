package com.mpos.lottery.te.gameimpl.toto.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.toto.dao.ToToTicketDao;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;

import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

/**
 * toto game sell ticket db handle
 */
@SuppressWarnings("unchecked")
public class ToToTicketDaoImpl extends BaseJpaDao implements ToToTicketDao {

    /**
     * get all matchs option type to use valid sell ticket selectteam param
     */
    @Override
    public List<String> getBetOptionType(final String gameId, final String drawNum) throws DataAccessException {
        String sql = "select to_char(sbo.type) from toto_game_instance tgi, sport_match_detail smd, "
                + "sport_bet_option sbo where tgi.game_instance_id = smd.game_instance_id  and "
                + "smd.bet_option_id = sbo.bet_option_id and tgi.draw_no = ?  and tgi.game_id = ? "
                + "order by smd.match_seq asc";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, drawNum);
        query.setParameter(2, gameId);
        return query.getResultList();
    }

    /**
     * get toto ticket information according to serialNo
     */
    @Override
    public List<ToToTicket> getToToTicketBySerialNo(String serialNo) throws DataAccessException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("serialNo", serialNo);
        return this.findByNamedParams("from ToToTicket t where t.serialNo = :serialNo ORDER BY t.id", param);
    }

    /**
     * get all toto triple info
     */
    @Override
    public Map<Integer, Integer[]> getToToOperatorParameters(final String gameId) throws DataAccessException {
        Map<Integer, Integer[]> result = new HashMap<Integer, Integer[]>();
        String sql = "select t.triple,t.min_double,t.max_double from TT_OPERATION_PARAMETERS t "
                + "where t.id = (select g.operation_parameters_id from game g where g.game_id = ?)";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, gameId);
        List list = query.getResultList();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Object[] objs = (Object[]) list.get(i);
                result.put(Integer.parseInt(objs[0].toString()), new Integer[] { Integer.parseInt(objs[1].toString()),
                        Integer.parseInt(objs[2].toString()) });
            }
        }
        return result;
    }

}
