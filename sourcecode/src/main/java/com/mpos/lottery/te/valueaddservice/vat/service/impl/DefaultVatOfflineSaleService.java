package com.mpos.lottery.te.valueaddservice.vat.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.config.exception.TeException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.OfflineTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.VAT;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Game;
import com.mpos.lottery.te.valueaddservice.vat.VatCompany;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.OperatorBizTypeDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2GameDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatCompanyDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;
import com.mpos.lottery.te.valueaddservice.vat.service.VatOfflineSaleService;
import com.mpos.lottery.te.valueaddservice.vat.web.VatOfflineSaleUploadDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatSaleTransactionDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;

import javax.annotation.Resource;

@Service("vatOfflineSaleService")
public class DefaultVatOfflineSaleService implements VatOfflineSaleService {
    private static Log logger = LogFactory.getLog(DefaultVatOfflineSaleService.class);
    private String messageExchangeName = MessagePack.PREFIX + ".451";
    @Resource(name = "uuidManager")
    private UUIDService uuidService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "operatorBizTypeDao")
    private OperatorBizTypeDao operatorBizTypeDao;
    @Resource(name = "vatCompanyDao")
    private VatCompanyDao vatCompanyDao;
    @Resource(name = "vat2GameDao")
    private Vat2GameDao vat2GameDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;
    @Resource(name = "vatOperatorBalanceDao")
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @Resource(name = "offlineRaffleTicketService")
    private OfflineTicketService raffleTicketService;
    @Resource(name = "offlineMagic100TicketService")
    private OfflineTicketService magic100TicketService;

    @Override
    public VatOfflineSaleUploadDto upload(Context respCtx, VatOfflineSaleUploadDto uploadDto)
            throws ApplicationException {
        Assert.notNull(uploadDto);
        // make sure TE won't be bleed by a single request.
        MLotteryContext sysContext = MLotteryContext.getInstance();
        int countOfTrans = sysContext.getInt("countoftrans.offlineupload", 100);
        if (countOfTrans < uploadDto.getVatSaleList().size()) {
            throw new ApplicationException(SystemException.CODE_EXCEED_MAX_TRANS_COUNT, "The max allowed trans is "
                    + countOfTrans + ", however there are total " + uploadDto.getVatSaleList().size()
                    + " trans in a uploading  request ");
        }

        // check the format
        if (uploadDto.getCount() != uploadDto.getVatSaleList().size()) {
            throw new MessageFormatException("The count(" + uploadDto.getCount()
                    + ") is unmatched with actual transactions in the request(" + uploadDto.getVatSaleList().size()
                    + ").");
        }

        VatOfflineSaleUploadDto respDto = new VatOfflineSaleUploadDto();
        BigDecimal vatTotalAmount = new BigDecimal("0");
        for (VatSaleTransactionDto clientVatTransDto : uploadDto.getVatSaleList()) {
            // handle VAT sale transaction one by one...ignore the failed one
            try {
                clientVatTransDto.mergeTransaction(respCtx.getTransaction());
                // generate unique ID
                clientVatTransDto.setId(this.getUuidService().getGeneralID());
                this.handleOffline(respCtx, clientVatTransDto);
                vatTotalAmount = vatTotalAmount.add(clientVatTransDto.getVatTotalAmount());
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                if (e instanceof TeException) {
                    TeException te = (TeException) e;
                    clientVatTransDto.setStatusCode(te.getErrorCode());
                } else {
                    clientVatTransDto.setStatus(SystemException.CODE_INTERNAL_SERVER_ERROR);
                }
                respDto.getVatSaleList().add(clientVatTransDto);
            }
        }
        respDto.setCount(uploadDto.getCount());
        respDto.setCountOfFailure(respDto.getVatSaleList().size());
        respDto.setCountOfSuccess(respDto.getCount() - respDto.getCountOfFailure());

        // update the vat balance...only count successful transaction
        VatOperatorBalance operatorBalance = this.getVatOperatorBalanceDao().findByOperatorIdForUpdate(
                respCtx.getTransaction().getOperatorId());
        operatorBalance.setSaleBalance(operatorBalance.getSaleBalance().add(vatTotalAmount));
        this.getBaseJpaDao().update(operatorBalance);

        return respDto;
    }

    // --------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------

    /**
     * TODO all those entities should be cached to avoid querying them repeatedly.
     */
    protected final void handleOffline(Context respCtx, VatSaleTransactionDto clientVatTransDto)
            throws ApplicationException {
        // check whether the VAT sale transaction exist already
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByRefNo(clientVatTransDto.getVatRefNo());
        if (hostVatTrans != null) {
            throw new ApplicationException(SystemException.CODE_DULPLICATED_TRANSACTION, "Entity("
                    + VatSaleTransaction.class + ") with refNo(" + clientVatTransDto.getVatRefNo()
                    + ") already existed.");
        }

        // lookup VAT
        VAT vat = this.getVatDao().findByCode(clientVatTransDto.getVatCode());
        if (vat == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by code("
                    + clientVatTransDto.getVatCode() + ")");
        }
        clientVatTransDto.setVatId(vat.getId());

        // determine the business type...B2B or B2C?
        OperatorBizType operatorBizType = this.getOperatorBizTypeDao().findByOperator(
                respCtx.getTransaction().getOperatorId());
        if (operatorBizType == null) {
            throw new SystemException("No operator BizType definition found by operatorId="
                    + respCtx.getTransaction().getOperatorId());
        }
        clientVatTransDto.setBusinessType(operatorBizType.getBusinessType());

        // lookup Game allocated to given VAT
        Vat2Game vat2Game = this.getVat2GameDao().findByVatAndBizType(vat.getId(), operatorBizType.getBusinessType());
        if (vat2Game == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOT_GAME_ALLOCATED,
                    "No Game has been allocated to Vat(id=" + vat.getId() + ") yet.");
        }
        Game game = this.getBaseJpaDao().findById(Game.class, vat2Game.getGameId());
        clientVatTransDto.setGameType(game.getType());
        clientVatTransDto.setVatRateTotalAmount(SimpleToolkit.mathMultiple(clientVatTransDto.getVatRate(),
                clientVatTransDto.getVatTotalAmount()));

        if (OperatorBizType.BIZ_B2B.equalsIgnoreCase(operatorBizType.getBusinessType())) {
            if (clientVatTransDto.getBuyerTaxNo() == null) {
                throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY, "For B2B operator(id="
                        + operatorBizType.getOperatorId() + "), the buyerTaxNo must be provided.");
            }

            // lookup seller and buyer company information
            VatCompany seller = this.getVatCompanyDao().findByMerchant(respCtx.getTransaction().getMerchantId());
            clientVatTransDto.setSellerCompanyId(seller.getId());
            VatCompany buyer = this.getVatCompanyDao().findByTaxNo(clientVatTransDto.getBuyerTaxNo());
            if (buyer == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "No merchant found by taxNo="
                        + clientVatTransDto.getBuyerTaxNo());
            }
            clientVatTransDto.setBuyerCompanyId(buyer.getId());
        }

        this.handleOfflineTicket(respCtx, clientVatTransDto);
        this.getBaseJpaDao().insert(new VatSaleTransaction(clientVatTransDto));
    }

    protected void handleOfflineTicket(Context respCtx, VatSaleTransactionDto clientVatTransDto)
            throws ApplicationException {
        if (clientVatTransDto.getTicketDto() == null) {
            return;
        }

        clientVatTransDto.setTicketSerialNo(clientVatTransDto.getTicketDto().getSerialNo());
        clientVatTransDto.setSaleTotalAmount(clientVatTransDto.getTicketDto().getTotalAmount());
        BaseTicket hostTicket = null;
        if (GameType.LUCKYNUMBER.getType() == clientVatTransDto.getGameType()) {
            Magic100Ticket mTicket = new Magic100Ticket(clientVatTransDto.getTicketDto());
            hostTicket = this.getMagic100TicketService().sync(respCtx, mTicket);
        } else if (GameType.RAFFLE.getType() == clientVatTransDto.getGameType()) {
            RaffleTicket rTicket = new RaffleTicket(clientVatTransDto.getTicketDto());
            hostTicket = this.getRaffleTicketService().sync(respCtx, rTicket);
        } else {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_GAME_TYPE, "Unsupported game type :"
                    + clientVatTransDto.getGameType());
        }
        clientVatTransDto.setGameInstanceId(hostTicket.getGameInstance().getId());
    }

    // --------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------------------------

    public OperatorBizTypeDao getOperatorBizTypeDao() {
        return operatorBizTypeDao;
    }

    public void setOperatorBizTypeDao(OperatorBizTypeDao operatorBizTypeDao) {
        this.operatorBizTypeDao = operatorBizTypeDao;
    }

    public VatCompanyDao getVatCompanyDao() {
        return vatCompanyDao;
    }

    public void setVatCompanyDao(VatCompanyDao vatCompanyDao) {
        this.vatCompanyDao = vatCompanyDao;
    }

    public Vat2GameDao getVat2GameDao() {
        return vat2GameDao;
    }

    public void setVat2GameDao(Vat2GameDao vat2GameDao) {
        this.vat2GameDao = vat2GameDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public VatDao getVatDao() {
        return vatDao;
    }

    public void setVatDao(VatDao vatDao) {
        this.vatDao = vatDao;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public VatSaleTransactionDao getVatSaleTransactionDao() {
        return vatSaleTransactionDao;
    }

    public void setVatSaleTransactionDao(VatSaleTransactionDao vatSaleTransactionDao) {
        this.vatSaleTransactionDao = vatSaleTransactionDao;
    }

    public VatOperatorBalanceDao getVatOperatorBalanceDao() {
        return vatOperatorBalanceDao;
    }

    public void setVatOperatorBalanceDao(VatOperatorBalanceDao vatOperatorBalanceDao) {
        this.vatOperatorBalanceDao = vatOperatorBalanceDao;
    }

    public OfflineTicketService getRaffleTicketService() {
        return raffleTicketService;
    }

    public void setRaffleTicketService(OfflineTicketService raffleTicketService) {
        this.raffleTicketService = raffleTicketService;
    }

    public OfflineTicketService getMagic100TicketService() {
        return magic100TicketService;
    }

    public void setMagic100TicketService(OfflineTicketService magic100TicketService) {
        this.magic100TicketService = magic100TicketService;
    }

    public String getMessageExchangeName() {
        return messageExchangeName;
    }

    public void setMessageExchangeName(String messageExchangeName) {
        this.messageExchangeName = messageExchangeName;
    }

}
