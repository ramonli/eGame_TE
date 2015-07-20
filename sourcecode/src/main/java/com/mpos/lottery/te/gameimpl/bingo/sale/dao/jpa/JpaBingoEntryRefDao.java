package com.mpos.lottery.te.gameimpl.bingo.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntryRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoEntryRefDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("bingoEntryRefDao")
public class JpaBingoEntryRefDao extends BaseJpaDao implements BingoEntryRefDao {

    @Override
    public List<BingoEntryRef> findByGameInstanceAndState(String gameInstanceId, int status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("status", status);
        List<BingoEntryRef> result = this.findByNamedParams(
                "from BingoEntryRef e where e.gameInstanceId=:gameInstanceId and e.status=:status", params);
        return result;
    }

    @Override
    public List<BingoEntryRef> findBySelectedNumber(String selectedNumber) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("selectedNumber", selectedNumber);
        List<BingoEntryRef> result = this.findByNamedParams(
                "from BingoEntryRef e where e.selectedNumber=:selectedNumber", params);
        return result;
    }
}
