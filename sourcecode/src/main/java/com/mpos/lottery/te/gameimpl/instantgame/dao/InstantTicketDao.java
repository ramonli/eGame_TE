package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;

import java.util.List;

public interface InstantTicketDao extends DAO {

    InstantTicket getBySerialNo(String ticketSerialNo);

    List<InstantTicket> getByPacketSerialNo(String packetSerialNo);

    List<InstantTicket> getByGameDrawNameAndBook(String gameInstanceName, String bookNumber);

    List<InstantTicket> getByRangeSerialNo(String beginSerialNo, String endSerialNo);
}
