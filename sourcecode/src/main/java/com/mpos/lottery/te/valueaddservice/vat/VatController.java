package com.mpos.lottery.te.valueaddservice.vat;

import com.google.gson.Gson;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.web.VatTransferDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.valueaddservice.vat.service.DownloadOfflineTicketService;
import com.mpos.lottery.te.valueaddservice.vat.service.VatCreditService;
import com.mpos.lottery.te.valueaddservice.vat.service.VatOfflineSaleService;
import com.mpos.lottery.te.valueaddservice.vat.service.VatReprintTicketService;
import com.mpos.lottery.te.valueaddservice.vat.service.VatSaleService;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatOfflineSaleUploadDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatReprintTicketReqDto;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class VatController {
    @Resource(name = "vatSaleService")
    private VatSaleService vatSaleService;
    @Resource(name = "vatOfflineSaleService")
    private VatOfflineSaleService vatOfflineSaleService;
    @Resource(name = "vatCreditService")
    private VatCreditService vatCreditService;
    @Resource(name = "vatReprintTicketService")
    private VatReprintTicketService vatReprintTicketService;
    @Resource(name = "downloadOfflineTicketService")
    private DownloadOfflineTicketService downloadOfflineTicketService;

    @RequestMap("{transType:200,gameType:-2}")
    public void sell(Context request, Context response) throws ApplicationException {
        VatSaleTransaction clientSale = (VatSaleTransaction) request.getModel();

        // the service will assemble client ticket.
        this.getVatSaleService().sell(response, clientSale);
        response.setModel(clientSale);
    }

    @RequestMap("{transType:450}")
    public void refund(Context request, Context response) throws ApplicationException {
        VatSaleTransaction clientSale = (VatSaleTransaction) request.getModel();

        // the service will assemble client ticket.
        this.getVatSaleService().refundVat(response, clientSale);
    }

    @RequestMap("{transType:451}")
    public void uploadOfflineSale(Context request, Context response) throws ApplicationException {
        VatOfflineSaleUploadDto clientSales = (VatOfflineSaleUploadDto) request.getModel();

        VatOfflineSaleUploadDto respDto = this.getVatOfflineSaleService().upload(response, clientSales);
        response.getTransaction().setTotalAmount(clientSales.calVatTotalAmount().subtract(respDto.calVatTotalAmount()));
        response.setModel(respDto);
    }

    @RequestMap("{transType:118}")
    public void vatTransfer(Context request, Context response) throws ApplicationException {
        VatTransferDto dto = (VatTransferDto) request.getModel();
        VatTransferDto respDto = this.getVatCreditService().vatTransferCredit(response, dto);
        response.setModel(respDto);

        response.getTransaction().setTotalAmount(dto.getAmount());
        // write transaction message for reversal.
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(response.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(dto));
        request.getTransaction().setTransMessage(transMsg);
    }

    @RequestMap("{transType:340}")
    public void downloadOfflineTicket(Context request, Context response) throws ApplicationException {
        OfflineTicketPackDto dto = (OfflineTicketPackDto) request.getModel();
        OfflineTicketPackDto respDto = this.getDownloadOfflineTicketService().downloadOfflineTicket(response, dto);
        response.setModel(respDto);
    }

    @RequestMap("{transType:119,gameType:14}")
    public void vatRaffleReprintTicket(Context request, Context response) throws ApplicationException {
        VatReprintTicketReqDto dto = (VatReprintTicketReqDto) request.getModel();
        VatSaleTransaction respDto = this.getVatReprintTicketService().raffleReprintTicket(request, response, dto);
        response.setModel(respDto);
    }

    @RequestMap("{transType:119,gameType:18}")
    public void vatMagicReprintTicket(Context request, Context response) throws ApplicationException {
        VatReprintTicketReqDto dto = (VatReprintTicketReqDto) request.getModel();
        VatSaleTransaction respDto = this.getVatReprintTicketService().MagicReprintTicket(request, response, dto);
        response.setModel(respDto);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public VatCreditService getVatCreditService() {
        return vatCreditService;
    }

    public void setVatCreditService(VatCreditService vatCreditService) {
        this.vatCreditService = vatCreditService;
    }

    public VatReprintTicketService getVatReprintTicketService() {
        return vatReprintTicketService;
    }

    public void setVatReprintTicketService(VatReprintTicketService vatReprintTicketService) {
        this.vatReprintTicketService = vatReprintTicketService;
    }

    public DownloadOfflineTicketService getDownloadOfflineTicketService() {
        return downloadOfflineTicketService;
    }

    public void setDownloadOfflineTicketService(DownloadOfflineTicketService downloadOfflineTicketService) {
        this.downloadOfflineTicketService = downloadOfflineTicketService;
    }

    public VatSaleService getVatSaleService() {
        return vatSaleService;
    }

    public void setVatSaleService(VatSaleService vatSaleService) {
        this.vatSaleService = vatSaleService;
    }

    public VatOfflineSaleService getVatOfflineSaleService() {
        return vatOfflineSaleService;
    }

    public void setVatOfflineSaleService(VatOfflineSaleService vatOfflineSaleService) {
        this.vatOfflineSaleService = vatOfflineSaleService;
    }

}
