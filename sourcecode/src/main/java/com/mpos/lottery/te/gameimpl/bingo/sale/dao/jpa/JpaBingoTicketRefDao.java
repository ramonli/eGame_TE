package com.mpos.lottery.te.gameimpl.bingo.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoTicketRefDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("bingoTicketRefDao")
public class JpaBingoTicketRefDao extends BaseJpaDao implements BingoTicketRefDao {

    @Override
    public List<BingoTicketRef> findByGameInstanceAndState(String gameInstanceId, int status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("status", status);
        List<BingoTicketRef> result = this.findByNamedParams(
                "from BingoTicketRef e where e.gameInstanceId=:gameInstanceId and e.status=:status", params);
        return result;
    }

    @Override
    public BingoTicketRef findBySerialNo(String serialNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", serialNo);
        List<BingoTicketRef> result = this.findByNamedParams(
                "from BingoTicketRef e where e.importTicketSerialNo=:serialNo", params);
        return this.single(result, false);
    }

    @Override
    public BingoTicketRef findByGameInstanceAndSequence(String gameInstanceId, long sequence) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("sequence", sequence);
        List<BingoTicketRef> result = this.findByNamedParams(
                "from BingoTicketRef e where e.gameInstanceId=:gameInstanceId and e.sequence=:sequence", params);
        return this.single(result, false);
    }
}
