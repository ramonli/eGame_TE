package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantVIRNPrizeDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstantVIRNPrizeDaoImpl extends BaseJpaDao implements InstantVIRNPrizeDao {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public InstantVIRNPrize getByGameDrawAndVIRN(String gameDrawId, String virn) {
        String sql = "from com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize i "
                + "where i.gameDraw=:gameDraw and i.virn=:virn";
        Map params = new HashMap();
        params.put("gameDraw", this.getEntityManager().getReference(InstantGameDraw.class, gameDrawId));
        params.put("virn", virn);

        List result = this.findByNamedParams(sql, params);
        if (result.size() == 0) {
            return null;
        }
        return (InstantVIRNPrize) result.get(0);
    }

}
