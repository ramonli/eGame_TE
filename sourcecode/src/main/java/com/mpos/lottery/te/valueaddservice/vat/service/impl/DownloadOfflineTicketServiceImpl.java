package com.mpos.lottery.te.valueaddservice.vat.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.OffLuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.OfflineCancellation;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.OffLuckyNumberSequenceDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.OfflinecancellationDao;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.game.dao.GameDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.VAT;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Game;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Merchant;
import com.mpos.lottery.te.valueaddservice.vat.dao.OperatorBizTypeDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2GameDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2MerchantDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.service.DownloadOfflineTicketService;
import com.mpos.lottery.te.valueaddservice.vat.web.NumberDto;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketDto;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.SelectedNumberPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.TicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatRefNoDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatRefNoPackDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

/**
 * @author terry
 * @version [3.3.1, 2014-7-25]
 */
public class DownloadOfflineTicketServiceImpl extends AbstractReversalOrCancelStrategy
        implements
            DownloadOfflineTicketService {
    private static Log logger = LogFactory.getLog(DownloadOfflineTicketServiceImpl.class);
    @Resource(name = "vatDao")
    private VatDao vatDao;

    @Resource(name = "vat2MerchantDao")
    private Vat2MerchantDao vat2MerchantDao;

    @Resource(name = "vat2GameDao")
    private Vat2GameDao vat2GameDao;

    @Resource(name = "operatorBizTypeDao")
    private OperatorBizTypeDao operatorBizTypeDao;

    @Resource(name = "offlinecancellationDao")
    private OfflinecancellationDao offlinecancellationDao;

    @Resource(name = "offLuckyNumberSequenceDao")
    private OffLuckyNumberSequenceDao offLuckyNumberSequenceDao;

    @Resource(name = "luckyNumberDao")
    private LuckyNumberDao luckyNumberDao;

    @Resource(name = "baseGameInstanceDao")
    private BaseGameInstanceDao gameInstanceDao;

    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;

    @Resource(name = "gameDao")
    private GameDao gameDao;

    private UUIDService uuidService;
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;

    @Override
    public OfflineTicketPackDto downloadOfflineTicket(Context respCtx, OfflineTicketPackDto offlineTicketPackDto)
            throws ApplicationException {
        OfflineTicketPackDto respOfflineTicketPackDto = new OfflineTicketPackDto();
        VAT vat = this.getVatDao().findByCode(offlineTicketPackDto.getVat().getCode());
        if (vat == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by code("
                    + offlineTicketPackDto.getVat().getCode() + ")");
        }

        respOfflineTicketPackDto.setVat(vat);

        // verify whether VAT has been allocated to merchant
        Vat2Merchant vat2Merchant = this.getVat2MerchantDao().findByVatAndMerchant(vat.getId(),
                respCtx.getTransaction().getMerchantId());
        if (vat2Merchant == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOT_ALLOCATED_TO_MERCHANT, "VAT(id=" + vat.getId()
                    + ") hasn't been allocated to merchant(id=" + respCtx.getTransaction().getMerchantId() + ") yet.");
        }

        // determine the business type of device first
        OperatorBizType deviceBizType = this.getOperatorBizTypeDao().findByOperator(
                respCtx.getTransaction().getOperatorId());
        if (deviceBizType == null) {
            throw new SystemException("No operator BizType definition found by operatorId="
                    + respCtx.getTransaction().getOperatorId());
        }
        // lookup Game allocated to given VAT
        Vat2Game vat2Game = this.getVat2GameDao().findByVatAndBizType(vat.getId(), deviceBizType.getBusinessType());
        if (vat2Game == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOT_GAME_ALLOCATED,
                    "No Game has been allocated to Vat(id=" + vat.getId() + ") yet.");
        }
        Game game = gameDao.findByGamdId(vat2Game.getGameId());
        Merchant distributeMerchant = merchantDao.findDistributeMerchantByMerchantId(respCtx.getTransaction()
                .getMerchantId());

        logger.info("Game Type is [" + game.getType() + "]");
        // set SelectedNumberPack
        if (game.getType() == Game.TYPE_LUCKYNUMBER && offlineTicketPackDto.getSelectedNumberPackDto() != null
                && offlineTicketPackDto.getSelectedNumberPackDto().getRequestCount() > 0) {
            List<Magic100GameInstance> list = gameInstanceDao.lookupActiveByGame(respCtx.getTransaction()
                    .getMerchantId(), Magic100GameInstance.class, vat2Game.getGameId());
            if (null == list || list.size() == 0) {
                throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW, "No active draw( merchantId="
                        + respCtx.getTransaction().getMerchantId() + ",gameId=" + vat2Game.getGameId() + ") yet.");

            }
            Magic100GameInstance magic100GameInstance = list.get(0);
            int maxNumberSeq = luckyNumberDao.getMaxNumberSeq(magic100GameInstance.getId());

            if (offlineTicketPackDto.getSelectedNumberPackDto().getRequestCount() > maxNumberSeq
                    || offlineTicketPackDto.getSelectedNumberPackDto().getRequestCount() > distributeMerchant
                            .getMaxOfflineTickets()) {
                throw new ApplicationException(SystemException.CODE_NO_LARGE_FOR_NUMBERS, "Number["
                        + offlineTicketPackDto.getSelectedNumberPackDto().getRequestCount()
                        + "] requests can not be greater than MaxOfflineTickets["
                        + distributeMerchant.getMaxOfflineTickets() + "] or maxNumberSeq[" + maxNumberSeq + "]");
            }
            SelectedNumberPackDto selectedNumberPackDto = this.getReservedNumbers(respCtx,
                    magic100GameInstance.getId(), vat2Game.getGameId(), maxNumberSeq,
                    offlineTicketPackDto.getSelectedNumberPackDto());
            respOfflineTicketPackDto.setSelectedNumberPackDto(selectedNumberPackDto);
        }

        // set VatRefNoPack 批量生成serialNo，barcode，validationCode
        if (offlineTicketPackDto.getTicketPackDto() != null
                && offlineTicketPackDto.getTicketPackDto().getRequestCount() > 0) {
            long requestCount = offlineTicketPackDto.getTicketPackDto().getRequestCount();
            if (requestCount > distributeMerchant.getMaxOfflineTickets()) {
                throw new ApplicationException(SystemException.CODE_NO_LARGE_FOR_NUMBERS, "Ticket[" + requestCount
                        + "] requests can not be greater than MaxOfflineTickets["
                        + distributeMerchant.getMaxOfflineTickets() + "] ");
            }

            TicketPackDto ticketPackDto = new TicketPackDto();
            List<OfflineTicketDto> tickets = new ArrayList();
            ticketPackDto.setRequestCount(requestCount);
            ticketPackDto.setTickets(tickets);
            for (long i = 0; i < requestCount; i++) {
                OfflineTicketDto clientTicket = new OfflineTicketDto();
                clientTicket.setRawSerialNo(this.getUuidService().getTicketSerialNo(TicketSerialSpec.OFFLINE_MODE,
                        game.getType()));
                clientTicket.setBarcode(new Barcoder(game.getType(), clientTicket.getRawSerialNo()).getBarcode());
                clientTicket.setValidationCode(BaseTicket.generateValidationCode());

                tickets.add(clientTicket);
            }

            respOfflineTicketPackDto.setTicketPackDto(ticketPackDto);
        }

        // set TicketPack 批量生成refNo
        if (offlineTicketPackDto.getVatRefNoPackDto() != null
                && offlineTicketPackDto.getVatRefNoPackDto().getRequestCount() > 0) {
            long requestCount = offlineTicketPackDto.getVatRefNoPackDto().getRequestCount();
            if (requestCount > distributeMerchant.getMaxOfflineTickets()) {
                throw new ApplicationException(SystemException.CODE_NO_LARGE_FOR_NUMBERS, "RefNo[" + requestCount
                        + "] requests can not be greater than MaxOfflineTickets["
                        + distributeMerchant.getMaxOfflineTickets() + "] ");
            }
            VatRefNoPackDto vatRefNoPackDto = new VatRefNoPackDto();
            vatRefNoPackDto.setRequestCount(requestCount);
            List<VatRefNoDto> vatRefNoDtos = new ArrayList();
            vatRefNoPackDto.setVatRefNoDtos(vatRefNoDtos);
            for (long i = 0; i < requestCount; i++) {
                VatRefNoDto vatRefNoDto = new VatRefNoDto();
                vatRefNoDto.setRefNo(this.getUuidService().getReferenceNo(TicketSerialSpec.OFFLINE_MODE));

                vatRefNoDtos.add(vatRefNoDto);
            }
            respOfflineTicketPackDto.setVatRefNoPackDto(vatRefNoPackDto);
        }

        return respOfflineTicketPackDto;
    }

    /**
     * @param respCtx
     *            The context represents the response.
     * @param dto
     *            Get the number of numbers reserved
     * @return SelectedNumberPackDto
     * @throws ApplicationException
     */
    @Override
    public SelectedNumberPackDto getReservedNumbers(Context respCtx, String magic100GameInstanceId, String gameId,
            long maxNumberSeq, SelectedNumberPackDto dto) throws ApplicationException {
        // 封装Offlinere quest Numbers
        List<NumberDto> numberDtos = doOfflineRequestNumbers(respCtx, gameId, dto.getRequestCount(), maxNumberSeq,
                respCtx.getTransactionID());

        // 设置每个号码的中奖金额
        assemblePrizeNumber(numberDtos, magic100GameInstanceId);

        SelectedNumberPackDto selectedNumberPackDto = new SelectedNumberPackDto();
        selectedNumberPackDto.setRequestCount(dto.getRequestCount());
        selectedNumberPackDto.setNumberDtos(numberDtos);
        return selectedNumberPackDto;
    }

    /*
     * 第一:先在从OfflineCancellation表下载号码 第二：如果OfflineCancellation表没有或不够用户请求的号码数，就从正式offline pirze表中下载
     */
    private List<NumberDto> doOfflineRequestNumbers(Context respCtx, String gameId, long requestNumbers,
            long maxNumberSeq, String teTransactionId) throws ApplicationException {
        List<NumberDto> list = new ArrayList();
        List<OfflineCancellation> offlineRerverseMsgList = new ArrayList();
        OffLuckyNumberSequence offLuckyNumberSequence = offLuckyNumberSequenceDao.findByGameId(gameId);
        this.getEntityManager().refresh(offLuckyNumberSequence, LockModeType.PESSIMISTIC_READ);

        List<OfflineCancellation> offlineCancellationList = offlinecancellationDao.findByGameId(gameId);
        long remainingRequestNumbers = requestNumbers;
        // 第一先在从OfflineCancellation表下载号码
        if (offlineCancellationList != null && offlineCancellationList.size() != 0) {
            for (OfflineCancellation offlineCancellation : offlineCancellationList) {
                remainingRequestNumbers = requestNumbers - list.size();
                if (remainingRequestNumbers <= 0) {
                    break;
                }
                OfflineCancellation offlineRerverseMsg = calculateRemainingNumberPerOfflineCancellation(list,
                        offlineCancellation, maxNumberSeq, remainingRequestNumbers);
                offlineRerverseMsg.setTeTransactionId(teTransactionId);
                offlineRerverseMsg.setGameId(gameId);
                offlineRerverseMsgList.add(offlineRerverseMsg);
                remainingRequestNumbers = requestNumbers - list.size();

            }
        }

        // 第二：如果OfflineCancellation表没有或不够用户请求的号码数，就从正式offline pirze表中下载

        long nextSeq = offLuckyNumberSequence.getNextSequence();
        if (remainingRequestNumbers > 0) {
            long numbers = 0;
            if ((nextSeq + remainingRequestNumbers) > maxNumberSeq) {
                numbers = remainingRequestNumbers + nextSeq - maxNumberSeq - 1;
                addNumberDto(list, nextSeq - 1, maxNumberSeq);
                addNumberDto(list, 0, numbers);
            } else {
                numbers = nextSeq + remainingRequestNumbers - 1;
                addNumberDto(list, nextSeq - 1, numbers);
            }
            offLuckyNumberSequence.setNextSequence(numbers + 1);
            offLuckyNumberSequenceDao.update(offLuckyNumberSequence);

            OfflineCancellation offlineRerverseMsg = new OfflineCancellation();
            offlineRerverseMsg.setStartNumber(nextSeq);
            offlineRerverseMsg.setEndNumber(numbers);
            offlineRerverseMsg.setCurrentNumber(nextSeq - 1);
            offlineRerverseMsg.setIsHandled(OfflineCancellation.STATE_PROCESSING);
            offlineRerverseMsg.setTeTransactionId(teTransactionId);
            offlineRerverseMsg.setGameId(gameId);
            offlineRerverseMsgList.add(offlineRerverseMsg);
        }
        offlinecancellationDao.update(offlineCancellationList);

        // write transaction message for reversal.
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(respCtx.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(offlineRerverseMsgList));
        respCtx.getTransaction().setTransMessage(transMsg);

        return list;
    }

    /**
     * 从OfflineCancellation表取号码，并封装所需以后reserve数据, 如果获取号码后更新currentNumber和isHandled currentNumber已經被使用.
     * 
     * @param list
     * @param offlineCancellation
     *            所读取OfflineCancellation数据
     * @param maxNumberSeq
     *            游戏所能取的最大号码数
     * @param requestNumbers
     *            要取的号码数量
     * @return OfflineCancellation 以后reserve数据
     */
    OfflineCancellation calculateRemainingNumberPerOfflineCancellation(List<NumberDto> list,
            OfflineCancellation offlineCancellation, long maxNumberSeq, long requestNumbers) {
        long endNumber = offlineCancellation.getEndNumber();
        long currentNumber = offlineCancellation.getCurrentNumber();
        long startNumber = offlineCancellation.getStartNumber();
        // Check this offlineCancellation how many numbers you can use
        long numbers = 0;
        long remainingNumbers = 0;
        // 这个值判断取值是否轮回了
        long endTransmigrationNumber = 0;
        if (startNumber < endNumber) {
            remainingNumbers = endNumber - currentNumber;
            numbers = requestNumbers + currentNumber;
            if (remainingNumbers <= requestNumbers) {
                numbers = endNumber;
                offlineCancellation.setIsHandled(OfflineCancellation.STATE_DONE);
            }
            offlineCancellation.setCurrentNumber(numbers);

        }
        if (startNumber > endNumber) {
            // 如果maxNumberSeq,start,end,current有两种情况计算剩余号码，
            // 示例：maxNumberSeq=50,start=40,end=10,current=5,那么剩余号码计算：remainingNumbers=
            // endNumber-currentNumber=10-5=5;
            // :
            // maxNumberSeq=50,start=40,end=10,current=45,那么剩余号码计算：remainingNumbers
            // = maxNumberSeq-currentNumber+endNumber=50-45+10=15;

            remainingNumbers = (currentNumber < startNumber) ? (endNumber - currentNumber) : (maxNumberSeq
                    - currentNumber + endNumber);

            // 封裝offlineCancellation
            if (remainingNumbers <= requestNumbers) {
                offlineCancellation.setIsHandled(OfflineCancellation.STATE_DONE);
                offlineCancellation.setCurrentNumber(endNumber);
            } else {
                offlineCancellation.setCurrentNumber(currentNumber + requestNumbers);
                // 如果offlineCancellation.getCurrentNumber()>maxNumberSeq,说明已经超过一个轮回了，示例：
                // maxNumberSeq=50,start=40,end=10,current=45,requestNumbers=10
                // 已经超过maxNumberSeq=50，就要重新计算:currentNumber+requestNumbers-currentNumber
                if (offlineCancellation.getCurrentNumber() > maxNumberSeq) {
                    offlineCancellation.setCurrentNumber(currentNumber + requestNumbers - maxNumberSeq);
                }
            }

            // 設置NumberDto,numbers就是要添加號碼終點
            if (startNumber <= currentNumber) {
                if ((maxNumberSeq - currentNumber) >= requestNumbers) {
                    numbers = currentNumber + requestNumbers;
                } else {
                    numbers = (remainingNumbers <= requestNumbers)
                            ? endNumber
                            : (requestNumbers + currentNumber - maxNumberSeq);
                    addNumberDto(list, 0, numbers);
                    endTransmigrationNumber = numbers;

                    numbers = maxNumberSeq;
                }
            } else {
                numbers = (endNumber - currentNumber) <= remainingNumbers
                        ? endNumber
                        : (currentNumber + requestNumbers);
            }
        }
        offlineCancellation.setUpdateTime(new Date());
        addNumberDto(list, currentNumber, numbers);

        OfflineCancellation offlineRerverseMsg = new OfflineCancellation();
        offlineRerverseMsg.setStartNumber(currentNumber + 1);
        offlineRerverseMsg.setEndNumber(endTransmigrationNumber == 0 ? numbers : endTransmigrationNumber);
        offlineRerverseMsg.setCurrentNumber(currentNumber);
        offlineRerverseMsg.setIsHandled(OfflineCancellation.STATE_PROCESSING);
        return offlineRerverseMsg;

    }

    private void assemblePrizeNumber(List<NumberDto> list, String gameInstanceId) {
        List<LuckyNumber> luckyNumbers = luckyNumberDao.findByGameInstanceId(gameInstanceId);
        for (NumberDto numberDto : list) {
            for (LuckyNumber luckyNumber : luckyNumbers) {
                if (numberDto.getNumber() == luckyNumber.getSequenceOfNumber()) {
                    numberDto.setPrizeAmount(luckyNumber.getPrizeAmount());
                    numberDto.setLuckyNumber(luckyNumber.getLuckyNumber());
                    break;
                }
            }
        }
    }

    private void addNumberDto(List<NumberDto> list, long currentNumber, long endNumber) {
        int count = list.size();
        for (long i = currentNumber + 1; i <= endNumber; i++) {
            NumberDto numberDto = new NumberDto();
            numberDto.setNumber(i);
            numberDto.setSequence(++count);
            list.add(numberDto);
        }
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        boolean isCancelDecline = false;
        List<OfflineCancellation> list = offlinecancellationDao.findByTransactionId(targetTrans.getId());
        if (list != null && list.size() > 0) {
            isCancelDecline = true;
        }
        Type type = new TypeToken<List<OfflineCancellation>>() {
        }.getType();

        List<OfflineCancellation> clientlist = new Gson().fromJson(targetTrans.getTransMessage().getRequestMsg(), type);
        for (OfflineCancellation offlineCancellation : clientlist) {
            offlineCancellation.setId(this.getUuidService().getGeneralID());
            offlineCancellation.setIsHandled(OfflineCancellation.STATE_PROCESSING);
            offlineCancellation.setCreateTime(new Date());
            offlineCancellation.setUpdateTime(new Date());
        }
        offlinecancellationDao.insert(clientlist);
        return isCancelDecline;
    }

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.RESERVED_NUMBERS.getRequestType());
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

    public OperatorBizTypeDao getOperatorBizTypeDao() {
        return operatorBizTypeDao;
    }

    public void setOperatorBizTypeDao(OperatorBizTypeDao operatorBizTypeDao) {
        this.operatorBizTypeDao = operatorBizTypeDao;
    }

    public OfflinecancellationDao getOfflinecancellationDao() {
        return offlinecancellationDao;
    }

    public void setOfflinecancellationDao(OfflinecancellationDao offlinecancellationDao) {
        this.offlinecancellationDao = offlinecancellationDao;
    }

    public OffLuckyNumberSequenceDao getOffLuckyNumberSequenceDao() {
        return offLuckyNumberSequenceDao;
    }

    public void setOffLuckyNumberSequenceDao(OffLuckyNumberSequenceDao offLuckyNumberSequenceDao) {
        this.offLuckyNumberSequenceDao = offLuckyNumberSequenceDao;
    }

    public LuckyNumberDao getLuckyNumberDao() {
        return luckyNumberDao;
    }

    public void setLuckyNumberDao(LuckyNumberDao luckyNumberDao) {
        this.luckyNumberDao = luckyNumberDao;
    }

    public BaseGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(BaseGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public GameDao getGameDao() {
        return gameDao;
    }

    public void setGameDao(GameDao gameDao) {
        this.gameDao = gameDao;
    }

}
