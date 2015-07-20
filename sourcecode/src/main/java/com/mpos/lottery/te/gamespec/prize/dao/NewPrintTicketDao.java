package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;

public interface NewPrintTicketDao extends DAO {

    NewPrintTicket getByOldTicket(String oldTicketSerialNo);
}
