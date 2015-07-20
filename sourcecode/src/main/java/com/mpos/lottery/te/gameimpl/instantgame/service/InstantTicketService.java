package com.mpos.lottery.te.gameimpl.instantgame.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ConfirmBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTicketResult;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTickets;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.Packet;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.port.Context;

public interface InstantTicketService {

    /**
     * Set all tickets with same packet serialNO to active.
     */
    ActiveResult active(ActiveCriteria criteria) throws ApplicationException;

    /**
     * Active a instant ticket real-time. Below pre-condition must be fulfilled:
     * <ol>
     * <li>The ticket must exist in the backend.</li>
     * <li>The ticket is sold(sold to distributor).</li>
     * <li>The ticket can not be in blacklist.</li>
     * <li>The game draw is not inactive.</li>
     * <li>Current time is after gameDraw.startActivationTime.</li>
     * </ol>
     * 
     * @param ticket
     *            The instant ticket which specify the ticket serial number.
     * @return the handled ticket which specify the total amount.
     */
    InstantTicket active(InstantTicket ticket) throws ApplicationException;

    /**
     * Validate an IG ticket to verify whether it is a winning ticket. A ticket can win cash, object, or cash+object, it
     * depends on the prize logic definition of the requeseted game instance. For eGame algorithm, this operation will
     * calculate the prize level based on serialNo and VIRN, and then lookup concret prize by prize logic and level.
     * <p/>
     * The implementation has to always check whether a ticket wins a cash or object prize.
     * <p/>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>The ticket must exist in the backend.</li>
     * <li>The ticket is active.</li>
     * <li>The ticket can not be in blacklist.</li>
     * <li>Game draw is not 'block payout'.</li>
     * <li>Current time is before stop payout time.</li>
     * </ul>
     * <b>Post-Conditions</b>
     * <p/>
     * <ul>
     * <li>A successful call to this interface will return a InstantPayoutDto which contains detailed prize information.
     * </li>
     * <li>The ticket must be marked as 'validated' status.</li>
     * <li>The payout details, including cash and object, must be recorded. The prizeAmount/taxAmount of object
     * prize(table 'Payout_Detail') won't be counted in summary(table 'Payout') payout(only cash prize will be counted).
     * </li>
     * <li>The credit-level of all related merchants must be updated(commission must be counted into credit-level too).</li>
     * </ul>
     * <b>Usage Restrictions</b>
     * <ul>
     * <li>Authorization - No authorization required.</li>
     * <li>Concurrent Access - No concurrent access allowed from a single device.</li>
     * </ul>
     * 
     * @param payout
     *            The following components of this argument must be not null:inputChannel,ticket.serialNo,
     *            ticket.ticketXOR3, actualAmount(if requires client provide this field, based on system configuration).
     * @return detailed prize information, including cash, object prize.
     * @exception ApplicationException
     *                when encounter any business related exception.
     */
    PrizeLevelDto validate(Context ctx, PrizeLevelDto payout) throws ApplicationException;

    /**
     * Do validation in a batch.
     */
    InstantBatchPayoutDto batchValidate(Context ctx, InstantBatchPayoutDto payouts) throws ApplicationException;

    InstantOfflineTicketResult offlineInfoUpload(InstantOfflineTickets offlineTicket) throws ApplicationException;

    /**
     * Sell a instant ticket at real time. Below condition must be fulfilled:
     * <ol>
     * <li>The ticket must exist in the backend.</li>
     * <li>The ticket can NOT be in blacklist.</li>
     * <li>The ticket must be inactive.</li>
     * </ol>
     * After sale, ticket will be active, and isSold=true, isSold2Customer=true.
     */
    InstantTicket sell(InstantTicket ticket) throws ApplicationException;

    void receive(Packet packet) throws ApplicationException;

    /**
     * Enquiry the prize information of the given ticket.
     * 
     * @param ticket
     *            The ig ticket.
     * @param isWinnerCounted
     *            Need to update the number of winner of this prize?? only validation will update it.
     * @return the prize information of given ticket.
     * @throws ApplicationException
     *             when encounter any business related exception.
     */
    PrizeLevelDto enquiryPrize(Context ctx, InstantTicket ticket, boolean isWinnerCounted) throws ApplicationException;

    /**
     * Handle the batch upload of offline IG validation. Due to validation is performed offline, the backend can do
     * nothing even the validation is failed, for example unmatched prize amount. The essential of this service is
     * synchronize validation transactions to the backend, the backend will just record this validation transaction.
     * <p/>
     * The general logic flow will be as below:
     * <ol>
     * <li>Iterate on each validation in request.</li>
     * <li>The backend calculates the prize amount according to the VIRN provided by request.</li>
     * <li>If prize amount matches with the request:</li>
     * <ol type="a">
     * <li>If it is the first time this validation uploaded to the backend, simply record this validation.</li>
     * <li>Else the backend will record this validation, and mark it as '2(dup&unmatched)' in response.</li>
     * </ol>
     * <li>If prize amount is unmatched with the request:</li>
     * <ol>
     * <li>If it is the first time this validation uploaded to the backend, then record it to DB, and marks it as
     * '3(unmatched)' in response.</li>
     * <li>Else the backend will record this validation, and marks it as '1(dup&unmatched)' in response.</li>
     * </ol>
     * </ol> Even fail to handle one validation, this service will continue to handle other one.
     * <p/>
     * <b>Pre-Condition</b>
     * <p/>
     * None
     * <p/>
     * <b>Post-Condition</b>
     * <ol>
     * <li>Each validation should be persisted at the backend, even prize amount is unmatched.</li>
     * </ol>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - No authorization necessary.</li>
     * <li>Concurrent - There isn't any limitation on the number of concurrent accesses of this interface.</li>
     * </ul>
     * <b>Error Handling</b>
     * <p/>
     * None
     * 
     * @param ctx
     *            The transaction context.
     * @param req
     *            The request shiping offline IG validation. Refer to TE protocol document for information that which
     *            components of this argument are mandatory.
     * @throws ApplicationException
     */
    InstantBatchPayoutDto offlineBatchValidate(Context ctx, InstantBatchPayoutDto req) throws ApplicationException;

    /**
     * Complete each batch validation tansaction of a confiration batch.
     * */
    ConfirmBatchPayoutDto partialPackage(Context response, ConfirmBatchPayoutDto confirmBatchPayout)
            throws ApplicationException;

    /**
     * Users will submit all tickets uploaded by partial package transaction.
     */
    InstantBatchReportDto ConfirmBatchValidation(Context response, InstantBatchReportDto dto)
            throws ApplicationException;

    /**
     * Query the batch upload success and failure votes and return Actual amount ,tax amount.
     */
    InstantBatchReportDto getReportOfConfirmBatchValidation(Context ctx, InstantBatchReportDto dto)
            throws ApplicationException;

}
