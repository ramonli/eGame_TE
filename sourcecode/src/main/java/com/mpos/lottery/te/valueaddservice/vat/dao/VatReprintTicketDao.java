package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;

import org.springframework.dao.DataAccessException;

import java.util.List;

public interface VatReprintTicketDao extends DAO {
    /**
     * find raffle ticket by serial no.
     */
    List<RaffleTicket> findRaffleTicketBySerialNo(String encryptserialno) throws DataAccessException;

    void insertRaffleTicketByNewSerialNo(List<RaffleTicket> ticket, String serialnonew, String encryptSerialnonew,
            List<String> newid) throws DataAccessException;

    void updateRaffleOldStatusBySerialNo(String entryptserialno) throws DataAccessException;

    // magic 100 part
    List<Magic100Ticket> findMagicTicketBySerialNo(String serialno) throws DataAccessException;

    void insertMagicTicketByNewSerialNo(List<Magic100Ticket> ticket, String serialnonew, String encryptSerialnonew,
            List<String> newid) throws DataAccessException;

    List<Magic100Entry> findMagicEntryBySerialNo(String serialno) throws DataAccessException;

    void insertMagicEntryByNewSerialNo(List<Magic100Entry> ticket, String serialnonew, String encryptSerialnonew,
            List<String> newid) throws DataAccessException;

    void updateMagicOldStatusBySerialNo(String entryptserialno) throws DataAccessException;

    // public part
    NewPrintTicket findNewPrintedTicketByOldSerialNo(String oldencryptSerialno);

    void insertNewPrintTicket(String encryptOldSerialno, String encryptNewSerialno, String id)
            throws DataAccessException;

}
