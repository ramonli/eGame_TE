package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstantTicketDaoImpl extends BaseJpaDao implements InstantTicketDao {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<InstantTicket> getByPacketSerialNo(String packetSerialNo) {
        Map params = new HashMap();
        params.put("packetSerialNo", packetSerialNo);
        String q = "from InstantTicket t where t.packetSerialNo=:packetSerialNo";
        return this.findByNamedParams(q, params);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public InstantTicket getBySerialNo(String serialNo) {
        Map params = new HashMap();
        params.put("serialNo", serialNo);
        String q = "from InstantTicket t where t.serialNo=:serialNo";
        List<InstantTicket> tickets = this.findByNamedParams(q, params);
        if (tickets.size() == 0) {
            return null;
        }
        if (tickets.size() > 1) {
            throw new DataIntegrityViolationException("Error: find " + tickets.size()
                    + " instant tickets with same serialNO:" + serialNo);
        }
        return tickets.get(0);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<InstantTicket> getByGameDrawNameAndBook(String gameDrawName, String bookNumber) {
        String sql = "from InstantTicket t " + "where t.gameDraw.name=:gameDrawName and t.bookNumber=:bookNumber";
        Map map = new HashMap();
        map.put("gameDrawName", gameDrawName);
        map.put("bookNumber", bookNumber);
        return this.findByNamedParams(sql, map);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<InstantTicket> getByRangeSerialNo(String beginSerialNo, String endSerialNo) {
        String sql = "from InstantTicket t " + "where t.serialNo between :beginSerialNo and :endSerialNo";
        Map map = new HashMap();
        map.put("beginSerialNo", beginSerialNo);
        map.put("endSerialNo", endSerialNo);
        return this.findByNamedParams(sql, map);
    }

}
