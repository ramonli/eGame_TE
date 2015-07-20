package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPrintTicketDaoImpl extends BaseJpaDao implements NewPrintTicketDao {

    public NewPrintTicket getByOldTicket(String oldTicketSerialNo) {
        Map param = new HashMap();
        param.put("oldTicketSerialNo", oldTicketSerialNo);
        String sql = "from NewPrintTicket t where t.oldTicketSerialNo=:oldTicketSerialNo";
        List<NewPrintTicket> tickets = this.findByNamedParams(sql, param);
        if (tickets.size() > 1) {
            throw new SystemException("Found " + tickets.size() + " NewPrintTicket records from database, Illegal.");
        }
        if (tickets.size() == 0) {
            return null;
        }
        return tickets.get(0);
    }

}
