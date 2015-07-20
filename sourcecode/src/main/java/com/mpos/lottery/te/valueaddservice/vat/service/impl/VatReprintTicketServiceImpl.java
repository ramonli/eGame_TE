package com.mpos.lottery.te.valueaddservice.vat.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.valueaddservice.vat.VAT;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2GameDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2MerchantDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatReprintTicketDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;
import com.mpos.lottery.te.valueaddservice.vat.service.VatReprintTicketService;
import com.mpos.lottery.te.valueaddservice.vat.web.VatReprintTicketReqDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

public class VatReprintTicketServiceImpl implements VatReprintTicketService {
    private static Log logger = LogFactory.getLog(VatReprintTicketServiceImpl.class);

    @Resource(name = "raffleTicketService")
    private CompositeTicketService raffleTicketService;
    @Resource(name = "magic100SaleService")
    private CompositeTicketService magic100TicketService;
    @Resource(name = "uuidManager")
    private UUIDService uUIDService;
    @Resource(name = "vatReprintTicketDao")
    private VatReprintTicketDao vatReprintTicketDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;
    @Resource(name = "vat2MerchantDao")
    private Vat2MerchantDao vat2MerchantDao;
    @Resource(name = "vat2GameDao")
    private Vat2GameDao vat2GameDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;

    @Override
    public VatSaleTransaction raffleReprintTicket(Context reqCtx, Context respCtx, VatReprintTicketReqDto dto)
            throws ApplicationException {
        String serialnonew = uUIDService.getTicketSerialNo(GameType.RAFFLE.getType());
        String encryptSerialnoold = BaseTicket.encryptSerialNo(dto.getSerialNo());
        String encryptSerialnonew = BaseTicket.encryptSerialNo(serialnonew);

        // step 0 [lookup whether exist the printed ticket ,if had then response directly]
        NewPrintTicket printicket = vatReprintTicketDao.findNewPrintedTicketByOldSerialNo(encryptSerialnoold);
        String currentEncryptSerialno = new String(encryptSerialnonew); // set the new serial no
        if (printicket != null) {
            currentEncryptSerialno = new String(printicket.getNewTicketSerialNo());
        }

        if (printicket == null) {
            // step 1 [find the ticket]
            List<RaffleTicket> raffleTicketlist = vatReprintTicketDao.findRaffleTicketBySerialNo(encryptSerialnoold);
            if (raffleTicketlist == null || raffleTicketlist.size() <= 0) {
                throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket with serialNO="
                        + dto.getSerialNo());
            }
            // step 2 [insert new serial no ticket into ticket table]
            List<String> ticketidlist = new ArrayList<String>();
            for (int i = 0; i < raffleTicketlist.size(); i++) {
                ticketidlist.add(uUIDService.getGeneralID());
            }
            vatReprintTicketDao.insertRaffleTicketByNewSerialNo(raffleTicketlist, serialnonew, encryptSerialnonew,
                    ticketidlist);

            // step 3 [update the old ticket status to invalid]
            vatReprintTicketDao.updateRaffleOldStatusBySerialNo(encryptSerialnoold);

            // step 4 [insert relationship into new_ticket table]
            vatReprintTicketDao
                    .insertNewPrintTicket(encryptSerialnoold, encryptSerialnonew, uUIDService.getGeneralID());
        }
        // step 5 [inquiry the record for new ticket record]
        // BaseTicket returnTicket =
        // vatReprintTicketDao.findRaffleTicketBySerialNo(currentEncryptSerialno);
        RaffleTicket requestraffleticket = new RaffleTicket();
        requestraffleticket.setSerialNo(currentEncryptSerialno);
        RaffleTicket raffleTicket = (RaffleTicket) this.getRaffleTicketService().enquiry(respCtx, requestraffleticket,
                true);
        // GameDraw related
        // VAT related
        // lookup VAT
        VatSaleTransaction vatsaletransaction = vatSaleTransactionDao.findBySerialnoAndOperatorid(encryptSerialnoold,
                reqCtx.getOperatorId());
        if (vatsaletransaction == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by serialno("
                    + encryptSerialnoold + ")");
        }

        VatSaleTransaction vat = new VatSaleTransaction();
        if (raffleTicket != null) {
            //set game type
            raffleTicket.getGameInstance().setGameType(GameType.RAFFLE.getType());
            vat.setTicket(raffleTicket);
        }
        vat.setVatTotalAmount(vatsaletransaction.getVatTotalAmount());
        vat.setVatRate(vatsaletransaction.getVatRateTotalAmount());
        
        if (vatsaletransaction.getVatRefNo() != null 
            && !vatsaletransaction.getVatRefNo().equals("")) {
            vat.setVatRefNo(vatsaletransaction.getVatRefNo());
        }
        
        if (vatsaletransaction.getBuyerCompanyId() != null 
            && !vatsaletransaction.getBuyerCompanyId().equals("")) {
            vat.setBuyerTaxNo(vatsaletransaction.getBuyerCompanyId());
        }
         // lookup VAT code
        VAT vatobj = this.getVatDao().findById(VAT.class, vatsaletransaction.getVatId());
        if (vatobj == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by id("
                            + vatsaletransaction.getVatId() + ")");
        }
        vat.setVatCode(vatobj.getCode());

         //
        // raffleTicket.setVat(vat);
        return vat;
    }

    @Override
    public VatSaleTransaction MagicReprintTicket(Context reqCtx, Context respCtx, VatReprintTicketReqDto dto)
            throws ApplicationException {
        String serialnonew = uUIDService.getTicketSerialNo(GameType.LUCKYNUMBER.getType());
        String encryptSerialnoold = BaseTicket.encryptSerialNo(dto.getSerialNo());
        String encryptSerialnonew = BaseTicket.encryptSerialNo(serialnonew);

        // step 0 [lookup whether exist the printed ticket ,if had then response directly]
        NewPrintTicket printicket = vatReprintTicketDao.findNewPrintedTicketByOldSerialNo(encryptSerialnoold);
        String currentEncryptSerialno = new String(encryptSerialnonew); // set the new serial no
        if (printicket != null) {
            currentEncryptSerialno = new String(printicket.getNewTicketSerialNo());
        }

        if (printicket == null) {
            // step 1 [find the ticket]
            List<Magic100Ticket> magicTicketlist = vatReprintTicketDao.findMagicTicketBySerialNo(encryptSerialnoold);
            if (magicTicketlist == null || magicTicketlist.size() <= 0) {
                throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket with serialNO="
                        + dto.getSerialNo());
            }
            // step 2 [insert new serial no ticket into ticket table]
            List<String> ticketidlist = new ArrayList<String>();
            for (int i = 0; i < magicTicketlist.size(); i++) {
                ticketidlist.add(uUIDService.getGeneralID());
            }
            vatReprintTicketDao.insertMagicTicketByNewSerialNo(magicTicketlist, serialnonew, encryptSerialnonew,
                    ticketidlist);

            // step 3 [update the old ticket status to invalid]
            vatReprintTicketDao.updateMagicOldStatusBySerialNo(encryptSerialnoold);

            // step 4 [find entries]
            List<Magic100Entry> magicEntrylist = vatReprintTicketDao.findMagicEntryBySerialNo(encryptSerialnoold);
            if (magicEntrylist == null || magicEntrylist.size() <= 0) {
                throw new ApplicationException(SystemException.CODE_NO_ENTRIES, "can NOT find entries with serialNO="
                        + dto.getSerialNo());
            }

            // step 5 [insert new entry]
            List<String> entryidlist = new ArrayList<String>();
            for (int i = 0; i < magicEntrylist.size(); i++) {
                entryidlist.add(uUIDService.getGeneralID());
            }
            vatReprintTicketDao.insertMagicEntryByNewSerialNo(magicEntrylist, serialnonew, encryptSerialnonew,
                    entryidlist);

            // step 6 [insert relationship into new_ticket table]
            vatReprintTicketDao
                    .insertNewPrintTicket(encryptSerialnoold, encryptSerialnonew, uUIDService.getGeneralID());
        }
        // step 5 [inquiry the record for new ticket record]
        Magic100Ticket requestmagicticket = new Magic100Ticket();
        requestmagicticket.setSerialNo(currentEncryptSerialno);
        Magic100Ticket magicTicket = (Magic100Ticket) this.getMagic100TicketService().enquiry(respCtx,
                requestmagicticket, true);
        // GameDraw(Entry) related

        // VAT related
        // lookup VAT
        VatSaleTransaction vatsaletransaction = vatSaleTransactionDao.findBySerialnoAndOperatorid(encryptSerialnoold,
                reqCtx.getOperatorId());
        if (vatsaletransaction == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by serialno("
                    + encryptSerialnoold + ")");
        }

        VatSaleTransaction vat = new VatSaleTransaction();
        if (magicTicket != null) {
            //set game type
            magicTicket.getGameInstance().setGameType(GameType.LUCKYNUMBER.getType());
            vat.setTicket(magicTicket);
        }
        vat.setVatTotalAmount(vatsaletransaction.getVatTotalAmount());
        vat.setVatRate(vatsaletransaction.getVatRateTotalAmount());
        
        if (vatsaletransaction.getVatRefNo() != null
            && !vatsaletransaction.getVatRefNo().equals("")) {
            vat.setVatRefNo(vatsaletransaction.getVatRefNo());
        }
         
        if (vatsaletransaction.getBuyerCompanyId() != null 
            && !vatsaletransaction.getBuyerCompanyId().equals("")) {
            vat.setBuyerTaxNo(vatsaletransaction.getBuyerCompanyId());
        }
        // lookup VAT code
        VAT vatobj = this.getVatDao().findById(VAT.class, vatsaletransaction.getVatId());
        if (vatobj == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by id("
            + vatsaletransaction.getVatId() + ")");
        }
        vat.setVatCode(vatobj.getCode());
        //
        // magicTicket.setVat(vat);
        return vat;
    }

    // --------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------

    public VatReprintTicketDao getVatReprintTicketDao() {
        return vatReprintTicketDao;
    }

    public CompositeTicketService getRaffleTicketService() {
        return raffleTicketService;
    }

    public void setRaffleTicketService(CompositeTicketService raffleTicketService) {
        this.raffleTicketService = raffleTicketService;
    }

    public CompositeTicketService getMagic100TicketService() {
        return magic100TicketService;
    }

    public void setMagic100TicketService(CompositeTicketService magic100TicketService) {
        this.magic100TicketService = magic100TicketService;
    }

    public void setVatReprintTicketDao(VatReprintTicketDao vatReprintTicketDao) {
        this.vatReprintTicketDao = vatReprintTicketDao;
    }

    public UUIDService getUUIDService() {
        return uUIDService;
    }

    public void setUUIDService(UUIDService service) {
        uUIDService = service;
    }

    public VatDao getVatDao() {
        return vatDao;
    }

    public void setVatDao(VatDao vatDao) {
        this.vatDao = vatDao;
    }

    public Vat2MerchantDao getVat2MerchantDao() {
        return vat2MerchantDao;
    }

    public void setVat2MerchantDao(Vat2MerchantDao vat2MerchantDao) {
        this.vat2MerchantDao = vat2MerchantDao;
    }

    public Vat2GameDao getVat2GameDao() {
        return vat2GameDao;
    }

    public void setVat2GameDao(Vat2GameDao vat2GameDao) {
        this.vat2GameDao = vat2GameDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

}
