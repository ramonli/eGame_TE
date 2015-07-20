package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantGameDrawDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstantGameDrawDaoImpl extends BaseJpaDao implements InstantGameDrawDao {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public InstantGameDraw getByName(String name) {
        String sql = "from InstantGameDraw i where i.name=:name";
        Map param = new HashMap();
        param.put("name", name);
        List<InstantGameDraw> draws = this.findByNamedParams(sql, param);
        if (draws.size() == 0) {
            return null;
        }
        return draws.get(0);
    }

}
