package com.mpos.lottery.te.gameimpl.instantgame.service.impl;

import com.google.gson.Gson;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGBatchReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGFailedTicketsReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutDetailTempDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutTempDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantVIRNPrizeDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGBatchReport;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGFailedTicketsReport;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGPayoutDetailTemp;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGPayoutTemp;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ConfirmBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTicketResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTickets;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantTicketResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.Packet;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelItemDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.logic.ValidationStrategy;
import com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active.CriteriaActiveStrategy;
import com.mpos.lottery.te.gameimpl.instantgame.service.InstantTicketService;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.PrizeObjectDao;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.merchant.web.PayoutLevelAllowRequest;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

public class InstantTicketServiceImpl implements InstantTicketService {
    private Log logger = LogFactory.getLog(InstantTicketServiceImpl.class);

    private InstantTicketDao instantTicketDao;

    private InstantVIRNPrizeDao instantVIRNPrizeDao;

    private PayoutDao payoutDao;

    private SysConfigurationDao sysConfigurationDao;

    private ValidationStrategy validationStrategy;

    private CreditService creditService;

    private MerchantService merchantService;

    private CriteriaActiveStrategy criteriaActiveStrategy;

    private UUIDService uuidManager;

    private PrizeObjectDao prizeObjectDao;

    private PayoutDetailDao payoutDetailDao;

    private IGBatchReportDao iGBatchReportDao;

    private IGFailedTicketsReportDao iGFailedTicketsReportDao;

    private IGPayoutDetailTempDao iGPayoutDetailTempDao;

    private IGPayoutTempDao iGPayoutTempDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;
    @Resource(name = "payoutCommissionBalanceService")
    private CommissionBalanceService commissionBalanceService;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;

    /**
     * @see InstantTicketService#active(ActiveCriteria)
     */
    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Start to active ticket by criteria: " + criteria.toString());
        }

        ActiveResult result = this.getCriteriaActiveStrategy().active(criteria);
        if (logger.isDebugEnabled()) {
            logger.debug("The active result: " + result.toString());
        }
        return result;
    }

    /**
     * @see InstantTicketService#active(InstantTicket)
     */
    public InstantTicket active(InstantTicket ticket) throws ApplicationException {
        InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        if (hostTicket == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "can NOT find instant ticket with serialNO='" + ticket.getSerialNo() + ".");
        }
        ticket.setTotalAmount(hostTicket.getGameDraw().getTicketFaceValue());
        // transfer between InstantTicket and ActiveCriteria
        ActiveCriteria criteria = new ActiveCriteria();
        criteria.setType(ActiveCriteria.TYPE_BYSINGLETICKET);
        criteria.setValue(ticket.getSerialNo());
        criteria.setTrans(ticket.getTransaction());
        ActiveResult result = this.getCriteriaActiveStrategy().active(criteria);
        if (result.getCount() != 1) { // fail to active
            throw new ApplicationException(result.getErrorCode(), "Fail to active ticket(serialNo='"
                    + ticket.getSerialNo() + "').");
        }

        return ticket;
    }

    /**
     * @see InstantTicketService#offlineInfoUpload(InstantOfflineTickets)
     */
    public InstantOfflineTicketResult offlineInfoUpload(InstantOfflineTickets offlineTicket)
            throws ApplicationException {
        List<InstantTicket> tickets = offlineTicket.getTickets();
        List<InstantTicketResult> results = new ArrayList<InstantTicketResult>();
        for (InstantTicket ticket : tickets) {
            InstantTicketResult result = new InstantTicketResult();
            result.setSerialNo(ticket.getSerialNo());
            result.setCode(SystemException.CODE_OK);
            // just update ticket
            InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
            if (hostTicket == null) {
                logger.warn("can NOT find instant ticket with serialNO='" + ticket.getSerialNo() + ".");
                result.setCode(SystemException.CODE_NO_TICKET);
                result.setTotalAmount(new BigDecimal("0"));
                results.add(result);
                continue;
            }
            result.setTotalAmount(hostTicket.getGameDraw().getTicketFaceValue());
            hostTicket.setSoldToCustomer(true);
            hostTicket.setSoldTime(ticket.getSoldTime());
            // persist ticket
            this.getInstantTicketDao().update(hostTicket);
            results.add(result);
        }
        InstantOfflineTicketResult result = new InstantOfflineTicketResult();
        result.setResults(results);
        return result;
    }

    /**
     * Validate a ticket to ensure whether it wins a cash/object prize. When persists payout transaction, the process
     * must follow below rule:
     * <ul>
     * <li>The prize_amount/tax_amount of table 'PAYOUT' only count both cash and object prize.</li>
     * <li>All payout details, including cash prize and object prize, must be persisted into table 'PAYOUT_DETAIL'.</li>
     * </ul>
     * For example, a ticket wins both cash and object prize:
     * <table border="1">
     * <tr>
     * <td>prize type</td>
     * <td>price amount</td>
     * <td>tax_amount</td>
     * <td>prize name</td>
     * <td>number of prize</td>
     * </tr>
     * <tr>
     * <td>cash</td>
     * <td>500.0</td>
     * <td>100.0</td>
     * <td>_</td>
     * <td>_</td>
     * </tr>
     * <tr>
     * <td>object</td>
     * <td>2000.0</td>
     * <td>400.0</td>
     * <td>Seagull Digital Camera</td>
     * <td>2</td>
     * </tr>
     * </table>
     * <p>
     * After committed, 1 payout will be persisted in table 'PAYOUT'(both cash and object prize):
     * <table border="1">
     * <tr>
     * <td>id</td>
     * <td>prize amount</td>
     * <td>tax_amount</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>500.0</td>
     * <td>100.0</td>
     * </tr>
     * </table>
     * <p>
     * And 2 records in table 'PAYOUT_DETAIL':
     * <table border="1">
     * <tr>
     * <td>prize type</td>
     * <td>prize amount</td>
     * <td>tax amount</td>
     * <td>prize name</td>
     * <td>number of prize</td>
     * </tr>
     * <tr>
     * <td>cash</td>
     * <td>500.0</td>
     * <td>100.0</td>
     * <td>_</td>
     * <td>1(always be 1)</td>
     * </tr>
     * <tr>
     * <td>object</td>
     * <td>2000.0</td>
     * <td>400.0</td>
     * <td>Seagull Digital Camera</td>
     * <td>2</td>
     * </tr>
     * </table>
     * 
     * @see InstantTicketService#validate(Context, PrizeLevelDto)
     * @see #enquiryPrize(Context, InstantTicket, boolean)
     */
    public PrizeLevelDto validate(Context ctx, PrizeLevelDto payoutDto) throws ApplicationException {
        /**
         * Why we put validation logic in a new method 'doValidate()'? As this service method 'validate(...)' is
         * transaction awared, and there are another transactional method 'batchValidate(...)', just as the name
         * implies, 'batchValidate()' will call 'validate()' multiple times to handle ticket one by one. What is more is
         * failure of one ticket should not affect another ticket. That says even fail to validate one ticket, it should
         * keep validating the next ticket.
         * <p>
         * Let imagine a case if all validation logic is implemented in 'validate(...)' method. batch validation prepare
         * to validate 3 tickets, and the 1st is successful, however the 2nd encounter a failure and 'validate()' throw
         * out a ApplicationException which will mark current transaction as rollback-only......
         * <p>
         * Oh, actually the above case won't happen if Spring support transaction by dynamic proxy, as the proxy can
         * only wrap on interface, not method level(can achieve by ASM, byte code wave, proxy transaction for each
         * method). So when 'batchValidate()' call 'validate()', as they are in same instance, no transactional checking
         * will happen on 'validate()'.
         */
        return this.doValidate(ctx, payoutDto);
    }

    /**
     * @see InstantTicketService#batchValidate(Context, InstantBatchPayoutDto)
     */
    public InstantBatchPayoutDto batchValidate(Context ctx, InstantBatchPayoutDto batchPayout)
            throws ApplicationException {
        InstantBatchPayoutDto dto = new InstantBatchPayoutDto();
        boolean isSamePrizeType = true;
        for (PrizeLevelDto payout : batchPayout.getPayouts()) {
            PrizeLevelDto prizeLevel = null;
            try {
                prizeLevel = this.validate(ctx, payout);

                // all successful validations must be same prize type...WHY???
                // disable it!
                // if (prizeType == null)
                // prizeType = result.getPrizeType() + "";
                // else if (!prizeType.equals(result.getPrizeType())) {
                // isSamePrizeType = false;
                // break;
                // }
                // assemble result
                dto.setActualAmount(dto.getActualAmount().add(prizeLevel.getActualAmount()));
                dto.setTaxAmount(dto.getTaxAmount().add(prizeLevel.getTaxAmount()));
                dto.setTotalSuccess(dto.getTotalSuccess() + 1);
            }
            // one failed validation shouldn't affect other tickets
            catch (ApplicationException e) { // can and ONLY can handle
                                             // ApplicationException
                                             // write log
                logger.warn(e.getMessage(), e);
                if (prizeLevel == null) {
                    prizeLevel = new PrizeLevelDto();
                }
                prizeLevel.setErrorCode(e.getErrorCode());
                prizeLevel.setTicket(payout.getTicket());
                dto.setTotalFail(dto.getTotalFail() + 1);
            }
            if (prizeLevel != null) {
                dto.getPayouts().add(prizeLevel);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Finish handling validate ticket(serialNo=" + payout.getTicket().getSerialNo() + "):"
                        + payout.toString());
            }
        }
        if (!isSamePrizeType) {
            throw new ApplicationException(SystemException.CODE_BATCH_VALIDATION_NOT_SAME_TYPE,
                    "the prize type of all successful validation must be same type.");
        }
        return dto;
    }

    /**
     * @see InstantTicketService#receive(Packet)
     */
    public void receive(Packet packet) throws ApplicationException {
        // not yet implemented
    }

    /**
     * @see InstantTicketService#sell(InstantTicket)
     */
    public InstantTicket sell(InstantTicket ticket) throws ApplicationException {
        InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        if (hostTicket == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "can NOT find instant ticket with serialNO='" + ticket.getSerialNo() + ".");
        }
        // set gameId
        ticket.getTransaction().setGameId(hostTicket.getGameDraw().getGame().getId());
        // check if the ticket is damaged
        if (InstantTicket.PHYSICAL_STATUS_DAMAGED == hostTicket.getPhysicalStatus()) {
            throw new ApplicationException(SystemException.CODE_ONE_DAMAGETICKET, "Ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is damaged.");
        }
        this.verifyBlacklist(hostTicket);
        if (InstantTicket.STATUS_INACTIVE != hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_SELL_INACTIVETICKET,
                    "only inactive ticket can be sold realtime, current status:" + hostTicket.getStatus());
        }
        hostTicket.setStatus(InstantTicket.STATUS_ACTIVE);
        hostTicket.setSoldToCustomer(true);
        hostTicket.setSoldTime(new Date());

        // added by Lee,2010-07-23,
        hostTicket.setOperatorId(ticket.getTransaction().getOperatorId());
        hostTicket.setDevId(String.valueOf(ticket.getTransaction().getDeviceId()));
        hostTicket.setMerchantId(String.valueOf(ticket.getTransaction().getMerchantId()));

        this.getInstantTicketDao().update(hostTicket);
        // return dto
        ticket.setTotalAmount(hostTicket.getGameDraw().getTicketFaceValue());
        return ticket;
    }

    /**
     * This operation will calculate the detailed prize information of a given ticket. The prize calculation logic will
     * be illustrated by below diagram. <img src="../doc-files/IG validation prize calculation logic.png"/>
     */
    @Override
    public PrizeLevelDto enquiryPrize(Context respCtx, InstantTicket ticket, boolean isWinnerCounted)
            throws ApplicationException {
        InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        if (hostTicket == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "can NOT find instant ticket with serialNO='" + ticket.getSerialNo() + ".");
        }
        if (InstantTicket.STATUS_VALIDATED == hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_VALIDATE_REPEAT, "ticket(" + hostTicket.getSerialNo()
                    + ") is already validated.");
        }
        hostTicket.setTransaction(respCtx.getTransaction());
        hostTicket.setRawSerialNo(false, ticket.getRawSerialNo());
        PrizeLevelDto prize = this.getValidationStrategy().validate(hostTicket, ticket.getTicketXOR3(), true);
        // avoid hibernate lazy intialization of collection
        prize.calculateAmount();
        prize.setTicket(ticket);
        return prize;
    }

    @Override
    public InstantBatchPayoutDto offlineBatchValidate(Context ctx, InstantBatchPayoutDto req)
            throws ApplicationException {
        InstantBatchPayoutDto resultDto = new InstantBatchPayoutDto();
        for (PrizeLevelDto clientPrize : req.getPayouts()) {
            // iterate on each valiation transaction

            InstantTicket clientTicket = clientPrize.getTicket();
            InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(clientTicket.getSerialNo());
            if (hostTicket == null) {
                // TODO: need more handling??
                logger.warn("No instant ticket found at the backend by serialNO=" + clientTicket.getSerialNo());
                continue;
            }
            hostTicket.setTransaction(ctx.getTransaction());
            hostTicket.setRawSerialNo(false, clientTicket.getRawSerialNo());
            boolean hasValidated = InstantTicket.STATUS_VALIDATED == hostTicket.getStatus();
            boolean matched = true;
            // calcuate the prize amount
            PrizeLevelDto hostPrize = this.getValidationStrategy().validate(hostTicket, clientTicket.getTicketXOR3(),
                    false);
            hostPrize.calculateAmount();
            if (hostPrize.getPrizeAmount().compareTo(clientPrize.getPrizeAmount()) != 0) {
                logger.warn("The prize amount of ticket(serialNo=" + clientTicket.getSerialNo() + ") is "
                        + hostPrize.getPrizeAmount() + ", but client request " + clientPrize.getPrizeAmount());
                matched = false;

            }
            if (!hasValidated) {
                // mark the ticket as validated
                hostTicket.setStatus(InstantTicket.STATUS_VALIDATED);
                this.getInstantTicketDao().update(hostTicket);

                // generate payout records...even prize amount is unmatched,
                // payout must record the client's prize amount, as the cash has
                // been paid.
                clientPrize.calculateAmount();
                clientPrize.setLevelItems(hostPrize.getLevelItems());
                this.generatePayout(hostTicket, clientPrize);
            }

            clientPrize.setPrizeAmount(hostPrize.getPrizeAmount());
            clientPrize.setActualAmount(hostPrize.getActualAmount());
            clientPrize.setTaxAmount(hostPrize.getTaxAmount());
            if (hasValidated) {
                resultDto.getPayouts().add(clientPrize);
                if (matched) {
                    clientPrize.setStatusCode(PrizeLevelDto.STATUS_CODE_DUP_MATCHED);
                } else {
                    clientPrize.setStatusCode(PrizeLevelDto.STATUS_CODE_DUP_UNMATCHED);
                }
            } else {
                if (!matched) {
                    resultDto.getPayouts().add(clientPrize);
                    clientPrize.setStatusCode(PrizeLevelDto.STATUS_CODE_UNMATCHED);
                }
            }
        }
        return resultDto;
    }

    // ----------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------

    private PrizeLevelDto doValidate(Context ctx, PrizeLevelDto payoutDto) throws ApplicationException {
        InstantTicket ticket = payoutDto.getTicket();
        // check whether client needs to input actual amount
        SysConfiguration sysConf = this.getSysConfigurationDao().getSysConfiguration();
        if (sysConf.isNeedInputAmount() && payoutDto.getClientPrizeAmount() == null) {
            throw new ApplicationException(SystemException.CODE_NO_ACTUALAMOUNT,
                    "No actual amount is specified, please input prize amount for " + "ticket(serialNo="
                            + ticket.getSerialNo() + ").");
        }

        InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        if (hostTicket == null) {
            throw new ApplicationException(SystemException.CODE_NO_IG_TICKET_FOUND,
                    "can NOT find instant ticket with serialNO=" + ticket.getSerialNo());
        }
        hostTicket.setTransaction(ticket.getTransaction());
        hostTicket.setRawSerialNo(false, ticket.getRawSerialNo());
        // check whether the ticket is damaged
        if (InstantTicket.PHYSICAL_STATUS_DAMAGED == hostTicket.getPhysicalStatus()) {
            throw new ApplicationException(SystemException.CODE_ONE_DAMAGETICKET, "Ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is damaged.");
        }
        if (InstantTicket.STATUS_VALIDATED == hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_VALIDATE_REPEAT, "Ticket(serialNO="
                    + ticket.getSerialNo() + ") has been validated, can NOT validate it again.");
        }
        // check the status of ticket
        if (InstantTicket.STATUS_ACTIVE != hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_VALIDATE_NOACTIVETICKET, "Ticket(serialNo="
                    + ticket.getSerialNo() + ",status=" + hostTicket.getStatus()
                    + ") isn't active, can NOT validate it.");
        }
        this.verifyBlacklist(hostTicket);
        // check if the ticket is payout blocked
        InstantGameDraw gameDraw = hostTicket.getGameDraw();
        // check status of game
        if (Game.STATUS_ACTIVE != gameDraw.getGame().getState()) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id=" + gameDraw.getGame().getId()
                    + ") isn't active.");
        }
        if (gameDraw.getIsSuspendPayoutBlocked() == 1) {
            throw new ApplicationException(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, "Instant ticket(id="
                    + hostTicket.getId() + ") is payout blocked.");
        }
        // check stop payout time 'Stop payout time' is not necessary when
        // create a new game. The reason is actual 'Stop payout time' really
        // depends on game sales, its popularity, so usually no one can foresee
        // a 'stop payout time' at the moment of game creation
        if (gameDraw.getStopPayoutTime() != null) {
            Date current = new Date();
            if (current.after(gameDraw.getStopPayoutTime())) {
                throw new ApplicationException(SystemException.CODE_AFTER_STOPPAYOUTIME,
                        "Current time is after stop payout time of instant game draw(id=" + gameDraw.getId()
                                + "), can NOT validate ticket(serialNo=" + hostTicket.getSerialNo() + ").");
            }
        }
        // check whether the game instance name is same with GGG in ticket
        // serial
        InstantTicketSerialNo no = new InstantTicketSerialNo(BaseTicket.descryptSerialNo(hostTicket.getSerialNo()));
        if (!no.getGGG().equals(hostTicket.getGameDraw().getName())) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SERIALNO, "The GGG(" + no.getGGG()
                    + ") in ticket serialNo(" + hostTicket.getSerialNo() + ") isn't same with game instance name("
                    + hostTicket.getGameDraw().getName() + ").");
        }

        PrizeLevelDto prize = this.getValidationStrategy().validate(hostTicket, ticket.getTicketXOR3(), false);
        prize.setTicket(hostTicket);
        prize.calculateAmount();
        // compare the actual amount
        if (sysConf.isNeedInputAmount() && payoutDto.getClientPrizeAmount().compareTo(prize.getPrizeAmount()) != 0) {
            throw new ApplicationException(SystemException.CODE_NOTMATCH_ACTUALAMOUNT,
                    "Client input wrong prize amount for ticket(serialNo=" + ticket.getSerialNo() + "), host:"
                            + prize.getPrizeAmount() + ", client:" + payoutDto.getClientPrizeAmount());
        }

        // if the winning algorithm is eGame, then this operation must check
        // whether the merchant has privilege to perform payout of winning
        // levels...prize group checking
        if (hostTicket.getGameDraw().getValidationType() == InstantGameDraw.VALIDATION_TYPE_EGAME) {
            int gameType = hostTicket.getGameDraw().getGame().getType();
            Set<Integer> levels = new HashSet<Integer>();
            levels.add(prize.getPrizeLevel());
            this.getMerchantService().allowPayout(
                    ctx,
                    hostTicket.getGameDraw().getGame(),
                    new PayoutLevelAllowRequest[] { new PayoutLevelAllowRequest(levels, gameType,
                            PrizeGroupItem.GROUP_TYPE_IG) }, prize.getActualAmount());
        } else {
            this.getMerchantService().allowPayout(ctx, hostTicket.getGameDraw().getGame(), null,
                    prize.getActualAmount());
        }

        // ------------------------------------------------------------
        // NOTE: the above statements are checking the pre-condition of
        // validation, you can NOT update any entity there. Due to batch
        // validation will invoke this method, if fail to validate one ticket,
        // the
        // modification will be committed...In fact, this transaction (one
        // ticket validation) should be roll-back.
        // ------------------------------------------------------------

        // set gameId
        ticket.getTransaction().setGameId(gameDraw.getGame().getId());
        // update VIRN prize
        if (prize.getVirnPrize() != null) {
            prize.getVirnPrize().setValidated(true);
            this.getInstantVIRNPrizeDao().update(prize.getVirnPrize());
        }

        // update prize level for later reference if the validation algorithm is
        // 'VIRN', save VIRN to InstantTicket.ticketXOR3... 'reversal'
        // transaction will need this information set VIRN to ticketXOR3
        hostTicket.setTicketXOR3(ticket.getTicketXOR3());
        hostTicket.setStatus(InstantTicket.STATUS_VALIDATED);
        hostTicket.setPrizeLevel(prize.getPrizeLevel());
        hostTicket.setPrizeAmount(prize.getPrizeAmount());
        hostTicket.setTaxAmount(prize.getTaxAmount());
        this.getInstantTicketDao().update(hostTicket);

        // update prize_win_count, JPA runtime will merge it automatically when
        // commit.
        prize.setNumberOfWinner(prize.getNumberOfWinner() + 1);

        this.generatePayout(hostTicket, prize);

        // restore credit...only count cash amount
        BigDecimal creditAmount = prize.getCashActualAmount();
        ticket.getTransaction().setTotalAmount(creditAmount);
        Object updatedOperator = this.getCreditService().credit(ticket.getTransaction().getOperatorId(),
                ticket.getTransaction().getMerchantId(), creditAmount, hostTicket.getGameDraw().getGame().getId(),
                true, false, false, ticket.getTransaction());

        // 是否计算佣金
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()
                && ctx.getTransaction().getType() == TransactionType.VALIDATE_INSTANT_TICKET.getRequestType()) {
            BalanceTransactions tempBalanceTransactions = new Gson().fromJson(ticket.getTransaction().getTransMessage()
                    .getRequestMsg(), BalanceTransactions.class);
            BalanceTransactions operatorBalanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(ctx,
                    creditAmount);
            operatorBalanceTransactions.setCommissionAmount(tempBalanceTransactions.getCommissionAmount());
            operatorBalanceTransactions.setCommissionRate(tempBalanceTransactions.getCommissionRate());
            if (updatedOperator == null) {
                throw new SystemException(SystemException.CODE_OPERATOR_TOPUP_IGNORED, "THe payout to operator(id="
                        + ticket.getOperatorId() + " will be ignored.");
            } else {
                if (updatedOperator instanceof Merchant) {
                    Merchant merchant = (Merchant) updatedOperator;
                    BalanceTransactions merchantBalanceTransactions = balanceTransactionsDao
                            .assembleBalanceTransactions(ctx, creditAmount);
                    merchantBalanceTransactions.setOwnerId(String.valueOf(merchant.getId()));
                    merchantBalanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_MERCHANT);
                    balanceTransactionsDao.insert(merchantBalanceTransactions);
                }
            }
            balanceTransactionsDao.insert(operatorBalanceTransactions);
        }

        return prize;
    }

    private void verifyBlacklist(InstantTicket hostTicket) throws ApplicationException {
        if (hostTicket.isInBlacklist()) {
            throw new ApplicationException(SystemException.CODE_TICKET_INBLACKLIST, "Instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is in blacklist, it will be blocked.");
        }
    }

    /**
     * Assemble payout from a instant ticket.
     */
    private void generatePayout(InstantTicket hostTicket, PrizeLevelDto payoutDto) throws ApplicationException {
        // GENERATE PAYOUT
        Payout payout = new Payout();
        payout.setCreateTime(new Date());
        payout.setId(this.getUuidManager().getGeneralID());
        payout.setStatus(Payout.STATUS_PAID);
        payout.setTicketSerialNo(hostTicket.getSerialNo());
        // set after-tax prize amount to Payout
        // if prize.getTaxAmount() is null, it means no tax for this prize.
        // payout.setTotalAmount(payoutDto.getPrizeAmount().subtract(payoutDto.getTaxAmount()));
        // payout.setBeforeTaxTotalAmount(payoutDto.getPrizeAmount());
        payout.setTotalAmount(payoutDto.getCashActualAmount());
        payout.setBeforeTaxTotalAmount(payoutDto.getCashPrizeAmount());
        payout.setBeforeTaxObjectAmount(payoutDto.getPrizeAmount().subtract(payoutDto.getCashPrizeAmount()));
        payout.setNumberOfObject(payoutDto.getNumberOfObject());
        payout.setTransaction(hostTicket.getTransaction());
        payout.setInputChannel(payoutDto.getInputChannel());
        payout.setType(Payout.TYPE_WINNING);
        payout.setValid(true);

        payout.setOperatorId(hostTicket.getTransaction().getOperatorId());
        payout.setMerchantId((int) hostTicket.getTransaction().getMerchantId());
        payout.setDevId((int) hostTicket.getTransaction().getDeviceId());
        payout.setGameInstanceId(hostTicket.getGameDraw().getId());
        payout.setGameId(hostTicket.getGameDraw().getGame().getId());

        this.getPayoutDao().insert(payout);

        // GENERATE PAYOUT_DETAIL
        for (PrizeLevelItemDto prizeItem : payoutDto.getLevelItems()) {
            PayoutDetail payoutDetail = new PayoutDetail();
            payoutDetail.setId(this.getUuidManager().getGeneralID());
            payoutDetail.setPayoutId(payout.getId());
            if (PrizeLevelDto.PRIZE_TYPE_OBJECT == prizeItem.getPrizeType()) {
                payoutDetail.setObjectId(prizeItem.getObjectId());
                payoutDetail.setObjectName(prizeItem.getObjectName());
            }
            // set prize_amount to total_amount
            payoutDetail.setPrizeAmount(prizeItem.getPrizeAmount());
            // set actual_amount to cash_amount
            payoutDetail.setActualAmount(prizeItem.getActualAmount());
            payoutDetail.setNumberOfObject(prizeItem.getNumberOfObject());
            payoutDetail.setPayoutType(prizeItem.getPrizeType());
            payoutDetail.setUpdateTime(new Date());
            payoutDetail.setCreateTime(new Date());
            payoutDetail.setCreateBy(payout.getOperatorId());
            payoutDetail.setUpdateBy(payout.getOperatorId());
            this.getPayoutDetailDao().insert(payoutDetail);
        }
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public ValidationStrategy getValidationStrategy() {
        return validationStrategy;
    }

    public void setValidationStrategy(ValidationStrategy validationStrategy) {
        this.validationStrategy = validationStrategy;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public CriteriaActiveStrategy getCriteriaActiveStrategy() {
        return criteriaActiveStrategy;
    }

    public void setCriteriaActiveStrategy(CriteriaActiveStrategy criteriaActiveStrategy) {
        this.criteriaActiveStrategy = criteriaActiveStrategy;
    }

    public SysConfigurationDao getSysConfigurationDao() {
        return sysConfigurationDao;
    }

    public void setSysConfigurationDao(SysConfigurationDao sysConfigurationDao) {
        this.sysConfigurationDao = sysConfigurationDao;
    }

    public InstantVIRNPrizeDao getInstantVIRNPrizeDao() {
        return instantVIRNPrizeDao;
    }

    public void setInstantVIRNPrizeDao(InstantVIRNPrizeDao instantVIRNPrizeDao) {
        this.instantVIRNPrizeDao = instantVIRNPrizeDao;
    }

    public PrizeObjectDao getPrizeObjectDao() {
        return prizeObjectDao;
    }

    public void setPrizeObjectDao(PrizeObjectDao prizeObjectDao) {
        this.prizeObjectDao = prizeObjectDao;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    public IGBatchReportDao getiGBatchReportDao() {
        return iGBatchReportDao;
    }

    public void setiGBatchReportDao(IGBatchReportDao iGBatchReportDao) {
        this.iGBatchReportDao = iGBatchReportDao;
    }

    public IGFailedTicketsReportDao getiGFailedTicketsReportDao() {
        return iGFailedTicketsReportDao;
    }

    public void setiGFailedTicketsReportDao(IGFailedTicketsReportDao iGFailedTicketsReportDao) {
        this.iGFailedTicketsReportDao = iGFailedTicketsReportDao;
    }

    public IGPayoutDetailTempDao getiGPayoutDetailTempDao() {
        return iGPayoutDetailTempDao;
    }

    public void setiGPayoutDetailTempDao(IGPayoutDetailTempDao iGPayoutDetailTempDao) {
        this.iGPayoutDetailTempDao = iGPayoutDetailTempDao;
    }

    public IGPayoutTempDao getiGPayoutTempDao() {
        return iGPayoutTempDao;
    }

    public void setiGPayoutTempDao(IGPayoutTempDao iGPayoutTempDao) {
        this.iGPayoutTempDao = iGPayoutTempDao;
    }

    public ConfirmBatchPayoutDto partialPackage(Context response, ConfirmBatchPayoutDto confirmBatchPayout)
            throws ApplicationException {

        // TODO Auto-generated method stub
        ConfirmBatchPayoutDto dto = new ConfirmBatchPayoutDto();
        boolean isSamePrizeType = true;
        for (PrizeLevelDto payout : confirmBatchPayout.getPayouts()) {
            PrizeLevelDto prizeLevel = null;
            // it's in processing by another operators
            InstantTicket ticket = payout.getTicket();

            try {
                // 1,a ticket cannot be able to do validation when it's in
                // processing

                // 1,if a ticket has been handled ,will be skip
                IGPayoutTemp oldPayoutTemp = this.getiGPayoutTempDao().getPayoutTempByCondition(
                        confirmBatchPayout.getBatchNumber(), ticket.getOperatorId(), ticket.getSerialNo());

                if (oldPayoutTemp != null) {
                    // skip to handle current ticket
                } else {
                    // 2,a ticket cannot be able to do confirm batch validation
                    // when
                    if (!this.getiGPayoutTempDao().isUsedByAnotherOperatorId(ticket.getOperatorId(),
                            ticket.getSerialNo())) {

                        prizeLevel = this.doTemporayValidate(response, payout, confirmBatchPayout.getBatchNumber(),
                                true);

                        // all successful validations must be same prize
                        // type...WHY???
                        // disable it!
                        // if (prizeType == null)
                        // prizeType = result.getPrizeType() + "";
                        // else if (!prizeType.equals(result.getPrizeType())) {
                        // isSamePrizeType = false;
                        // break;
                        // }
                        // assemble result
                        dto.setActualAmount(dto.getActualAmount().add(prizeLevel.getActualAmount()));
                        dto.setTaxAmount(dto.getTaxAmount().add(prizeLevel.getTaxAmount()));
                        dto.setTotalSuccess(dto.getTotalSuccess() + 1);
                    } else {
                        // this tickets is failed,need to add it to failed
                        // ticket table

                        // failedReport.setBatchId(confirmBatchPayout.getBatchNumber());
                        // failedReport.setIgSerialNumber(ticket.getSerialNo());
                        // failedReport.setOperatorId(ticket.getOperatorId());
                        this.createFailedTicketData(SystemException.CODE_TICKET_IS_USING_BY_OTHER_OPERATOR,
                                confirmBatchPayout.getBatchNumber(), ticket.getSerialNo(), response.getOperatorId());
                    }
                }
            }
            // one failed validation shouldn't affect other tickets
            catch (ApplicationException e) { // can and ONLY can handle
                                             // ApplicationException
                                             // write log
                logger.warn(e.getMessage(), e);
                if (prizeLevel == null) {
                    prizeLevel = new PrizeLevelDto();
                }
                prizeLevel.setErrorCode(e.getErrorCode());
                prizeLevel.setTicket(payout.getTicket());
                dto.setTotalFail(dto.getTotalFail() + 1);
                this.createFailedTicketData(e.getErrorCode(), confirmBatchPayout.getBatchNumber(),
                        ticket.getSerialNo(), response.getOperatorId());
            }
            if (prizeLevel != null) {
                dto.getPayouts().add(prizeLevel);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Finish handling validate ticket(serialNo=" + payout.getTicket().getSerialNo() + "):"
                        + payout.toString());
            }
        }
        if (!isSamePrizeType) {
            throw new ApplicationException(SystemException.CODE_BATCH_VALIDATION_NOT_SAME_TYPE,
                    "the prize type of all successful validation must be same type.");
        }
        return dto;
    }

    private void createFailedTicketData(int errorCode, long batchNumber, String serialNo, String operatorId)
            throws ApplicationException {

        IGFailedTicketsReport failedReport = new IGFailedTicketsReport();
        failedReport.setBatchId(batchNumber);
        failedReport.setIgSerialNumber(serialNo);
        failedReport.setOperatorId(operatorId);
        failedReport.setStatus(Constants.YES);
        failedReport.setCreateTime(new Date());
        failedReport.setUpdateTime(new Date());
        failedReport.setId(this.getUuidManager().getGeneralID());
        failedReport.setErrorCode(errorCode);

        this.getiGFailedTicketsReportDao().insert(failedReport);
        // failedReport=this.getiGFailedTicketsReportDao().findById(IGFailedTicketsReport.class,
        // failedReport.getId());
    }

    /**
     * Need to complete validation temporarily for confirmation batch validation with a same batch number.
     */
    private PrizeLevelDto doTemporayValidate(Context ctx, PrizeLevelDto payoutDto, long batchNumber,
            boolean isIngoreCheckingAmount) throws ApplicationException {
        InstantTicket ticket = payoutDto.getTicket();

        // check whether client needs to input actual amount
        SysConfiguration sysConf = this.getSysConfigurationDao().getSysConfiguration();
        if (!isIngoreCheckingAmount && sysConf.isNeedInputAmount() && payoutDto.getClientPrizeAmount() == null) {
            throw new ApplicationException(SystemException.CODE_NO_ACTUALAMOUNT,
                    "No actual amount is specified, please input prize amount for " + "ticket(serialNo="
                            + ticket.getSerialNo() + ").");
        }

        InstantTicket hostTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        if (hostTicket == null) {
            throw new ApplicationException(SystemException.CODE_NO_IG_TICKET_FOUND,
                    "can NOT find instant ticket with serialNO=" + ticket.getSerialNo());
        }
        // TODO: need to hidden this ???????????????????
        hostTicket.setTransaction(ticket.getTransaction());// need to notify

        hostTicket.setRawSerialNo(false, ticket.getRawSerialNo());
        // check whether the ticket is damaged
        if (InstantTicket.PHYSICAL_STATUS_DAMAGED == hostTicket.getPhysicalStatus()) {
            throw new ApplicationException(SystemException.CODE_ONE_DAMAGETICKET, "Ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is damaged.");
        }
        if (InstantTicket.STATUS_VALIDATED == hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_VALIDATE_REPEAT, "Ticket(serialNO="
                    + ticket.getSerialNo() + ") has been validated, can NOT validate it again.");
        }
        // check the status of ticket
        if (InstantTicket.STATUS_ACTIVE != hostTicket.getStatus()
                || InstantTicket.STATUS_PROCESSING == hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_VALIDATE_NOACTIVETICKET, "Ticket(serialNo="
                    + ticket.getSerialNo() + ",status=" + hostTicket.getStatus()
                    + ") isn't active, can NOT validate it.");
        }
        this.verifyBlacklist(hostTicket);
        // check if the ticket is payout blocked
        InstantGameDraw gameDraw = hostTicket.getGameDraw();
        // check status of game
        if (Game.STATUS_ACTIVE != gameDraw.getGame().getState()) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id=" + gameDraw.getGame().getId()
                    + ") isn't active.");
        }
        if (gameDraw.getIsSuspendPayoutBlocked() == 1) {
            throw new ApplicationException(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, "Instant ticket(id="
                    + hostTicket.getId() + ") is payout blocked.");
        }
        // check stop payout time 'Stop payout time' is not necessary when
        // create a new game. The reason is actual 'Stop payout time' really
        // depends on game sales, its popularity, so usually no one can foresee
        // a 'stop payout time' at the moment of game creation
        if (gameDraw.getStopPayoutTime() != null) {
            Date current = new Date();
            if (current.after(gameDraw.getStopPayoutTime())) {
                throw new ApplicationException(SystemException.CODE_AFTER_STOPPAYOUTIME,
                        "Current time is after stop payout time of instant game draw(id=" + gameDraw.getId()
                                + "), can NOT validate ticket(serialNo=" + hostTicket.getSerialNo() + ").");
            }
        }
        // check whether the game instance name is same with GGG in ticket
        // serial
        InstantTicketSerialNo no = new InstantTicketSerialNo(BaseTicket.descryptSerialNo(hostTicket.getSerialNo()));
        if (!no.getGGG().equals(hostTicket.getGameDraw().getName())) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SERIALNO, "The GGG(" + no.getGGG()
                    + ") in ticket serialNo(" + hostTicket.getSerialNo() + ") isn't same with game instance name("
                    + hostTicket.getGameDraw().getName() + ").");
        }

        PrizeLevelDto prize = this.getValidationStrategy().validate(hostTicket, ticket.getTicketXOR3(), false);

        prize.setTicket(hostTicket);
        prize.calculateAmount();
        // compare the actual amount
        if (!isIngoreCheckingAmount && sysConf.isNeedInputAmount()
                && payoutDto.getClientPrizeAmount().compareTo(prize.getPrizeAmount()) != 0) {
            throw new ApplicationException(SystemException.CODE_NOTMATCH_ACTUALAMOUNT,
                    "Client input wrong prize amount for ticket(serialNo=" + ticket.getSerialNo() + "), host:"
                            + prize.getPrizeAmount() + ", client:" + payoutDto.getClientPrizeAmount());
        }

        // if the winning algorithm is eGame, then this operation must check
        // whether the merchant has privilege to perform payout of winning
        // levels...prize group checking
        if (hostTicket.getGameDraw().getValidationType() == InstantGameDraw.VALIDATION_TYPE_EGAME) {
            int gameType = hostTicket.getGameDraw().getGame().getType();
            Set<Integer> levels = new HashSet<Integer>();
            levels.add(prize.getPrizeLevel());
            this.getMerchantService().allowPayout(
                    ctx,
                    hostTicket.getGameDraw().getGame(),
                    new PayoutLevelAllowRequest[] { new PayoutLevelAllowRequest(levels, gameType,
                            PrizeGroupItem.GROUP_TYPE_IG) }, prize.getActualAmount());
        } else {
            this.getMerchantService().allowPayout(ctx, hostTicket.getGameDraw().getGame(), null,
                    prize.getActualAmount());
        }

        // ------------------------------------------------------------
        // NOTE: the above statements are checking the pre-condition of
        // validation, you can NOT update any entity there. Due to batch
        // validation will invoke this method, if fail to validate one ticket,
        // the
        // modification will be committed...In fact, this transaction (one
        // ticket validation) should be roll-back.
        // ------------------------------------------------------------

        // set gameId
        ticket.getTransaction().setGameId(gameDraw.getGame().getId());
        // update VIRN prize
        if (prize.getVirnPrize() != null) {
            prize.getVirnPrize().setValidated(true);
            this.getInstantVIRNPrizeDao().update(prize.getVirnPrize());
        }

        // update prize level for later reference if the validation algorithm is
        // 'VIRN', save VIRN to InstantTicket.ticketXOR3... 'reversal'
        // transaction will need this information set VIRN to ticketXOR3
        hostTicket.setTicketXOR3(ticket.getTicketXOR3());
        // need set status to process
        hostTicket.setStatus(InstantTicket.STATUS_PROCESSING);
        hostTicket.setPrizeLevel(prize.getPrizeLevel());
        hostTicket.setPrizeAmount(prize.getPrizeAmount());
        hostTicket.setTaxAmount(prize.getTaxAmount());

        this.getInstantTicketDao().update(hostTicket);

        // update prize_win_count, JPA runtime will merge it autlmatically when
        // commit.
        prize.setNumberOfWinner(prize.getNumberOfWinner() + 1);

        this.generatePayoutTemp(hostTicket, prize, batchNumber);

        // restore credit...only count cash amount
        /* Need to handle in confirmation batch Validation.TODO: */
        /*
         * BigDecimal creditAmount = prize.getCashActualAmount(); this.getCreditService
         * ().credit(ticket.getTransaction().getOperatorId(), ticket.getTransaction().getMerchantId(), creditAmount,
         * hostTicket.getGameDraw().getGame().getId(), true, false, false);
         */

        // need to calculate

        return prize;
    }

    /**
     * Assemble temp payout from a batch instant ticket.
     */
    private void generatePayoutTemp(InstantTicket hostTicket, PrizeLevelDto payoutDto, long batchNumber)
            throws ApplicationException {
        boolean isSucceeded = true;
        // check whether the data is existed in DB already,if it's existed,don't
        // need to store it into

        // GENERATE PAYOUT
        IGPayoutTemp payoutTemp = new IGPayoutTemp();
        payoutTemp.setCreateTime(new Date());
        payoutTemp.setId(this.getUuidManager().getGeneralID());
        payoutTemp.setStatus(Payout.STATUS_PAID);
        payoutTemp.setTicketSerialNo(hostTicket.getSerialNo());
        // set after-tax prize amount to Payout
        // if prize.getTaxAmount() is null, it means no tax for this prize.
        // payout.setTotalAmount(payoutDto.getPrizeAmount().subtract(payoutDto.getTaxAmount()));
        // payout.setBeforeTaxTotalAmount(payoutDto.getPrizeAmount());
        payoutTemp.setTotalAmount(payoutDto.getCashActualAmount());
        payoutTemp.setBeforeTaxTotalAmount(payoutDto.getCashPrizeAmount());
        payoutTemp.setBeforeTaxObjectAmount(payoutDto.getPrizeAmount().subtract(payoutDto.getCashPrizeAmount()));
        payoutTemp.setNumberOfObject(payoutDto.getNumberOfObject());
        payoutTemp.setTransaction(hostTicket.getTransaction());
        payoutTemp.setInputChannel(payoutDto.getInputChannel());
        payoutTemp.setType(Payout.TYPE_WINNING);
        payoutTemp.setValid(true);

        payoutTemp.setOperatorId(hostTicket.getTransaction().getOperatorId());
        payoutTemp.setMerchantId((int) hostTicket.getTransaction().getMerchantId());
        payoutTemp.setDevId((int) hostTicket.getTransaction().getDeviceId());
        payoutTemp.setGameInstanceId(hostTicket.getGameDraw().getId());
        payoutTemp.setGameId(hostTicket.getGameDraw().getGame().getId());

        payoutTemp.setiGBatchNumber(batchNumber);

        this.getiGPayoutTempDao().insert(payoutTemp);

        // GENERATE PAYOUT_DETAIL
        for (PrizeLevelItemDto prizeItem : payoutDto.getLevelItems()) {
            IGPayoutDetailTemp payoutDetailTemp = new IGPayoutDetailTemp();
            payoutDetailTemp.setId(this.getUuidManager().getGeneralID());
            payoutDetailTemp.setPayoutId(payoutTemp.getId());
            if (PrizeLevelDto.PRIZE_TYPE_OBJECT == prizeItem.getPrizeType()) {
                payoutDetailTemp.setObjectId(prizeItem.getObjectId());
                payoutDetailTemp.setObjectName(prizeItem.getObjectName());
            }
            // set prize_amount to total_amount
            payoutDetailTemp.setPrizeAmount(prizeItem.getPrizeAmount());
            // set actual_amount to cash_amount
            payoutDetailTemp.setActualAmount(prizeItem.getActualAmount());
            payoutDetailTemp.setNumberOfObject(prizeItem.getNumberOfObject());
            payoutDetailTemp.setPayoutType(prizeItem.getPrizeType());
            payoutDetailTemp.setUpdateTime(new Date());
            payoutDetailTemp.setCreateTime(new Date());
            payoutDetailTemp.setCreateBy(payoutTemp.getOperatorId());
            payoutDetailTemp.setUpdateBy(payoutTemp.getOperatorId());

            payoutDetailTemp.setiGBatchNumber(batchNumber);
            payoutDetailTemp.setOperatorId(payoutTemp.getOperatorId());

            this.getiGPayoutDetailTempDao().insert(payoutDetailTemp);

        }
        // return isSucceeded;
    }

    /**
     * Query the batch upload success and failure votes and return Actual amount ,tax amount.
     */
    @Override
    public InstantBatchReportDto getReportOfConfirmBatchValidation(Context ctx, InstantBatchReportDto dto)
            throws ApplicationException {
        InstantBatchReportDto instantBatchReportDto = new InstantBatchReportDto();
        IGBatchReport igBatchReport = iGBatchReportDao.getByBatchId(ctx.getOperatorId(), dto.getBatchNumber());
        if (igBatchReport == null) {
            throw new ApplicationException(SystemException.CODE_NOT_EXIST_BATCH_NUMBER, "Batch number["
                    + dto.getBatchNumber() + "] not exist!!");
        }
        instantBatchReportDto.setActualAmount(igBatchReport.getActualAmount());
        instantBatchReportDto.setTaxAmount(igBatchReport.getTaxAmount());
        instantBatchReportDto.setBatchNumber(igBatchReport.getBatchId());
        instantBatchReportDto.setTotalFail(igBatchReport.getFailedTicketsCount());
        instantBatchReportDto.setTotalSuccess(igBatchReport.getSucceededTicketsCount());

        List<IGFailedTicketsReport> list = iGFailedTicketsReportDao.findByBatchId(ctx.getOperatorId(),
                dto.getBatchNumber());
        if (null != list && list.size() > 0) {
            for (IGFailedTicketsReport igFailedTicketsReport : list) {
                InstantTicket instantTicket = new InstantTicket();
                instantTicket.setRawSerialNo(BaseTicket.descryptSerialNo(igFailedTicketsReport.getIgSerialNumber()));
                instantTicket.setErrorCode(igFailedTicketsReport.getErrorCode());
                instantBatchReportDto.getTickets().add(instantTicket);
                instantBatchReportDto.setTotalFail(instantBatchReportDto.getTotalFail() + 1);
            }
        }

        return instantBatchReportDto;
    }

    @Override
    public InstantBatchReportDto ConfirmBatchValidation(Context response, InstantBatchReportDto dto)
            throws ApplicationException {
        InstantBatchReportDto instantBatchReportDto = new InstantBatchReportDto();
        IGBatchReport igBatchReport = iGBatchReportDao.getByBatchId(response.getOperatorId(), dto.getBatchNumber());
        if (igBatchReport == null) {
            // if this batch number was not confirm before
            /*
             * 1,Check whether there are partial records for this batch, 2,Get all temp payout records
             */
            BigDecimal totalAmount = this.getiGPayoutTempDao().getTotoalAmountBeforeTax(dto.getBatchNumber(),
                    response.getOperatorId());
            BigDecimal actualAmout = this.getiGPayoutTempDao().getActualAmount(dto.getBatchNumber(),
                    response.getOperatorId());
            Long succeededTicketsCount = this.getiGPayoutTempDao().getSucceededTicketsCount(dto.getBatchNumber(),
                    response.getOperatorId());
            // 3,Generate new batchreport record

            igBatchReport = new IGBatchReport();
            igBatchReport.setActualAmount(totalAmount);
            igBatchReport.setTaxAmount(totalAmount.subtract(actualAmout));
            igBatchReport.setOperatorId(response.getOperatorId());
            igBatchReport.setBatchId(dto.getBatchNumber());
            igBatchReport.setCreateTime(new Date());
            igBatchReport.setUpdateTime(new Date());
            igBatchReport.setId(this.getUuidManager().getGeneralID());
            igBatchReport.setSucceededTicketsCount(succeededTicketsCount);

            this.getiGBatchReportDao().insert(igBatchReport);
            // 4,update ig ticket records to valid
            this.getiGPayoutTempDao().validateAllTicket(dto.getBatchNumber(), response.getOperatorId());
            // 5,add payout records
            this.getiGPayoutTempDao().movePayoutData(dto.getBatchNumber(), response.getOperatorId());
            // 6,add payout detail records
            this.getiGPayoutDetailTempDao().movePayoutDetailData(dto.getBatchNumber(), response.getOperatorId());
            // 7.
            response.getTransaction().setTotalAmount(actualAmout);
            Object operatorOrMerchant = this.getBalanceService().balance(response, response.getTransaction(),
                    BalanceService.BALANCE_TYPE_PAYOUT, response.getTransaction().getOperatorId(), true);
            /*
             * 8,update operator's payout credit
             */
            // 8.1 get all games of this batch
            List<Game> list = this.getiGPayoutTempDao().getAllGamesOfThisBatch(dto.getBatchNumber(),
                    response.getOperatorId());
            if (list != null && list.size() > 0) {
                Map<String, Object> map = new HashMap<String, Object>();

                List<GsonGame> items = new LinkedList<GsonGame>();

                for (Game game : list) {
                    BigDecimal creditAmount = this.getiGPayoutTempDao().getActualAmountByGame(game,
                            dto.getBatchNumber(), response.getOperatorId());
                    // 1,deduct balance

                    response.getTransaction().setGameId(game.getId());
                    // // 2,calculating commission
                    this.getCommissionBalanceService().calCommission(response, operatorOrMerchant);

                    // adding trace logs
                    GsonGame item = new GsonGame();
                    item.setGameId(game.getId());
                    item.setPrizeAmount(creditAmount);
                    items.add(item);

                    /*
                     * this.getCreditService().credit(response.getTransaction().getOperatorId(),
                     * response.getTransaction().getMerchantId(), new BigDecimal(creditAmount), game.getId(), true,
                     * false, false);
                     */

                }
                map.put("gameStat", items);
                response.getTransaction().setGameId("");

                TransactionMessage transMessage = new TransactionMessage();
                transMessage.setTransactionId(response.getTransaction().getId());
                transMessage.setResponseMsg(new Gson().toJson(map));
                response.getTransaction().setTransMessage(transMessage);

                // response.getTransaction().getTransMessage().setResponseMsg();

            }
            // 8.remove temporary data

        }
        instantBatchReportDto.setActualAmount(igBatchReport.getActualAmount());
        instantBatchReportDto.setTaxAmount(igBatchReport.getTaxAmount());
        instantBatchReportDto.setBatchNumber(igBatchReport.getBatchId());
        instantBatchReportDto.setTotalFail(igBatchReport.getFailedTicketsCount());
        instantBatchReportDto.setTotalSuccess(igBatchReport.getSucceededTicketsCount());

        List<IGFailedTicketsReport> list = iGFailedTicketsReportDao.findByBatchId(response.getOperatorId(),
                dto.getBatchNumber());
        if (null != list && list.size() > 0) {
            for (IGFailedTicketsReport igFailedTicketsReport : list) {
                InstantTicket instantTicket = new InstantTicket();
                instantTicket.setRawSerialNo(BaseTicket.descryptSerialNo(igFailedTicketsReport.getIgSerialNumber()));
                instantTicket.setErrorCode(igFailedTicketsReport.getErrorCode());
                instantBatchReportDto.getTickets().add(instantTicket);
                instantBatchReportDto.setTotalFail(instantBatchReportDto.getTotalFail() + 1);
            }
        }

        // TODO Auto-generated method stub
        return instantBatchReportDto;
    }

    public CommissionBalanceService getCommissionBalanceService() {
        return commissionBalanceService;
    }

    public void setCommissionBalanceService(CommissionBalanceService commissionBalanceService) {
        this.commissionBalanceService = commissionBalanceService;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    private static class GsonGame {

        String gameId;
        BigDecimal prizeAmount;

        public String getGameId() {
            return gameId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public BigDecimal getPrizeAmount() {
            return prizeAmount;
        }

        public void setPrizeAmount(BigDecimal prizeAmount) {
            this.prizeAmount = prizeAmount;
        }

    }
}
