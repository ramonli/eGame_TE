package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatReprintTicketDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

public class VatReprintTicketDaoImpl extends BaseJpaDao implements VatReprintTicketDao {
    private Log logger = LogFactory.getLog(VatReprintTicketDaoImpl.class);

    // =====================================
    // ====Raffle===========================

    @Override
    public List<RaffleTicket> findRaffleTicketBySerialNo(String encryptserialno) throws DataAccessException {
        String sql = "select * from RA_TE_TICKET where SERIAL_NO=?";
        Query query = this.getEntityManager().createNativeQuery(sql, RaffleTicket.class);
        query.setParameter(1, encryptserialno);
        List list = query.getResultList();
        if (list != null && list.size() > 0) {
            return (List<RaffleTicket>) list;
        }
        return null;
    }

    @Override
    public void insertRaffleTicketByNewSerialNo(List<RaffleTicket> ticketlist, String serialnonew,
            String encryptSerialnonew, List<String> newid) throws DataAccessException {
        List<RaffleTicket> inserticketlist = new ArrayList<RaffleTicket>();
        for (int i = 0; i < ticketlist.size(); i++) {
            RaffleTicket ticket = ticketlist.get(i);
            RaffleTicket newTicket = new RaffleTicket();
            newTicket = (RaffleTicket) ticket.clone();
            Barcoder barcode = new Barcoder(GameType.RAFFLE.getType(), serialnonew);
            // use newTicket object change the values
            newTicket.setId(newid.get(i));
            newTicket.setSerialNo(encryptSerialnonew);
            newTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
            newTicket.setBarcode(false, barcode.getBarcode());
            newTicket.setUpdateTime(new Date());
            inserticketlist.add(newTicket);
        }
        this.insert(inserticketlist);
    }

    @Override
    public void updateRaffleOldStatusBySerialNo(String entryptserialno) throws DataAccessException {
        List<RaffleTicket> list = findRaffleTicketBySerialNo(entryptserialno);
        // set value
        for (RaffleTicket ticket : list) {
            ticket.setStatus(BaseTicket.STATUS_INVALID); // ticket status set as 0 invalid
        }
        this.update(list);
    }

    // =====================================
    // ====Magic100=========================

    @Override
    public List<Magic100Ticket> findMagicTicketBySerialNo(String encryptserialno) throws DataAccessException {
        String sql = "select * from LK_TE_TICKET where SERIAL_NO=?";
        Query query = this.getEntityManager().createNativeQuery(sql, Magic100Ticket.class);
        query.setParameter(1, encryptserialno);
        List list = query.getResultList();
        if (list != null && list.size() > 0) {
            return (List<Magic100Ticket>) list;
        }
        return null;
    }

    @Override
    public void insertMagicTicketByNewSerialNo(List<Magic100Ticket> ticketlist, String serialnonew,
            String encryptSerialnonew, List<String> newid) throws DataAccessException {
        List<Magic100Ticket> inserticketlist = new ArrayList<Magic100Ticket>();
        for (int i = 0; i < ticketlist.size(); i++) {
            Magic100Ticket ticket = ticketlist.get(i);
            Magic100Ticket newTicket = new Magic100Ticket();
            newTicket = (Magic100Ticket) ticket.clone();
            Barcoder barcode = new Barcoder(GameType.LUCKYNUMBER.getType(), serialnonew);
            // use newTicket object change the values
            newTicket.setId(newid.get(i));
            newTicket.setSerialNo(encryptSerialnonew);
            newTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
            newTicket.setBarcode(false, barcode.getBarcode());
            newTicket.setUpdateTime(new Date());
            inserticketlist.add(newTicket);
        }
        this.insert(inserticketlist);
    }

    @Override
    public List<Magic100Entry> findMagicEntryBySerialNo(String encryptserialno) throws DataAccessException {
        String sql = "select * from LK_TE_ENTRY where TICKET_SERIALNO=?";
        Query query = this.getEntityManager().createNativeQuery(sql, Magic100Entry.class);
        query.setParameter(1, encryptserialno);
        List list = query.getResultList();
        if (list != null && list.size() > 0) {
            return (List<Magic100Entry>) list;
        }
        return null;
    }

    @Override
    public void insertMagicEntryByNewSerialNo(List<Magic100Entry> ticketlist, String serialnonew,
            String encryptSerialnonew, List<String> newid) throws DataAccessException {
        List<Magic100Entry> inserticketlist = new ArrayList<Magic100Entry>();
        for (int i = 0; i < ticketlist.size(); i++) {
            Magic100Entry ticket = ticketlist.get(i);
            Magic100Entry newTicket = new Magic100Entry();
            newTicket = (Magic100Entry) ticket.clone();
            // use newTicket object change the values
            newTicket.setId(newid.get(i));
            newTicket.setTicketSerialNo(encryptSerialnonew);
            newTicket.setUpdateTime(new Date());
            inserticketlist.add(newTicket);
        }
        this.insert(inserticketlist);
    }

    @Override
    public void updateMagicOldStatusBySerialNo(String entryptserialno) throws DataAccessException {
        List<Magic100Ticket> list = findMagicTicketBySerialNo(entryptserialno);
        // set value
        for (Magic100Ticket ticket : list) {
            ticket.setStatus(BaseTicket.STATUS_INVALID); // ticket status set as 0 invalid
        }
        this.update(list);
    }

    // =====================================
    // ====public=========================

    @Override
    public NewPrintTicket findNewPrintedTicketByOldSerialNo(String oldencryptSerialno) {
        String sql = "select * from NEWPRINT_TICKET where OLD_TICKET_SERIALNO=?";
        Query query = this.getEntityManager().createNativeQuery(sql, NewPrintTicket.class);
        query.setParameter(1, oldencryptSerialno);
        List printticketlist = query.getResultList();
        if (printticketlist != null && printticketlist.size() > 0) {
            return (NewPrintTicket) printticketlist.get(0);
        }
        return null;
    }

    @Override
    public void insertNewPrintTicket(String encryptOldSerialno, String encryptNewSerialno, String id)
            throws DataAccessException {
        NewPrintTicket newprinticket = new NewPrintTicket();
        newprinticket.setId(id);
        newprinticket.setOldTicketSerialNo(encryptOldSerialno);
        newprinticket.setNewTicketSerialNo(encryptNewSerialno);
        newprinticket.setVersion(0);
        newprinticket.setStatus(2); // vat reprint ticket
        newprinticket.setCreateTime(new Date());
        newprinticket.setUpdateTime(new Date());

        this.insert(newprinticket);

    }

}
