package com.mpos.lottery.te.gamespec.prize.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameTypeAware;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategy;

public interface PrizeService extends ReversalOrCancelStrategy, GameTypeAware {

    /**
     * Enquiry the prize of a ticket. A ticket may win both normal and sencond chance prize, the implementation must
     * calculate both of them.
     * <p>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>The sold draw must be 'payout started'.</li>
     * </ul>
     * <b>Post-Condition</b>
     * <ul>
     * <li>A successful call to this interface will return both normal and second chance prize information.</li>
     * </ul>
     * <b>Usage-Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by signed in clients.</li>
     * <li>Concurrent Access - There isn't any limitation on the number of concurrent access of this interface. The
     * service is indempotent and won't change any states.</li>
     * </ul>
     * Only when game draw is 'PAYOUT STARTED', the enquiry can get the prize response. To multlple-draw ticket, all
     * 'PAYOUT STARTED' game draw should be calculated, the other game draw(not 'payout started', such as 'new', \
     * 'active' etc) should be regarded as 'return'.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param clientTicket
     *            The ticket which is claiming prize. The following components must not be null: serialNo.
     * @return both normal and second chance prize information.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    PrizeDto enquiry(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException;

    /**
     * This operation will pay the winning ticket. One ticket can be paid only once, and operator will collect the
     * winning ticket after payout. To a multiple-draw ticket, there will be multiple ticket records, also one ticket
     * record will has one corresponding 'PAYOUT' record if this ticket record wins a prize(even return is a payout).
     * <p>
     * This operator will call {@link #enquiry(Context, BaseTicket)} to get prize information first.
     * <p>
     * Let's say there are a 3-draws ticket, in which ticket#1 associate draw#1(payout started), and ticket#2 associates
     * with draw#2 which is active(or now, it doesn't matter).
     * <table border="1">
     * <tr>
     * <td>TicketID</td>
     * <td>Ticket SerialNO</td>
     * <td>Ticket Status</td>
     * <td>Is Count In Pool</td>
     * <td>Game Instance ID</td>
     * <td>GameInstance Status</td>
     * </tr>
     * <tr>
     * <td>t1</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d1</td>
     * <td>payout started</td>
     * </tr>
     * <tr>
     * <td>t2</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t3</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * </table>
     * <h1>Print New Ticket Mode</h1>
     * After payout, only tickets belong to a payout-started game draw will be set as 'paid'(here is t1), all other
     * ticket records will be remained as 'accepted' and count_in_pool.
     * <p>
     * Also new tickets will be generated for those tickets in future game instances, here they are t2 and t3.
     * <p>
     * <table border="1">
     * <tr>
     * <td>TicketID</td>
     * <td>Ticket SerialNo</td>
     * <td>Ticket Status</td>
     * <td>Is Count In Pool</td>
     * <td>Game Instance ID</td>
     * <td>GameInstance Status</td>
     * </tr>
     * <tr>
     * <td>t1</td>
     * <td>SN-OLD</td>
     * <td>paid</td>
     * <td>true</td>
     * <td>d1</td>
     * <td>payout started</td>
     * </tr>
     * <tr>
     * <td>t2</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t3</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * <tr>
     * <td>t4</td>
     * <td>SN-NEW</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t5</td>
     * <td>SN-NEW</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * </table>
     * And the log of new printed ticket will be:
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>SerialNo of OldTicket</td>
     * <td>SerialNO of NewTicket</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>SN-OLD</td>
     * <td>SN-NEW</td>
     * <td>Wait Confirmation</td>
     * </tr>
     * </table>
     * And payout entity will be:
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>Ticket SN</td>
     * <td>Game Instance</td>
     * <td>Type</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>P1</td>
     * <td>SN-OLD</td>
     * <td>d1</td>
     * <td>Winning</td>
     * <td>Paid</td>
     * </tr>
     * </table>
     * <p>
     * Then a 'confirm payout(205)' request will be issued by client, when got this request, TE will be very sure that
     * client has handled the 'payout' response successfully. After confirmation, the status will be:
     * <table border="1">
     * <tr>
     * <td>TicketID</td>
     * <td>Ticket SerialNo</td>
     * <td>Ticket Status</td>
     * <td>Is Count In Pool</td>
     * <td>Transaction Type</td>
     * <td>Game Instance ID</td>
     * <td>GameInstance Status</td>
     * </tr>
     * <tr>
     * <td>t1</td>
     * <td>SN-OLD</td>
     * <td>paid</td>
     * <td>true</td>
     * <td>200</td>
     * <td>d1</td>
     * <td>payout started</td>
     * </tr>
     * <tr>
     * <td>t2</td>
     * <td>SN-OLD</td>
     * <td>invalid</td>
     * <td>false</td>
     * <td>200</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t3</td>
     * <td>SN-OLD</td>
     * <td>invalid</td>
     * <td>false</td>
     * <td>200</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * <tr>
     * <td>t4</td>
     * <td>SN-NEW</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>302</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t5</td>
     * <td>SN-NEW</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>302</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * </table>
     * And the log of new printed ticket will be:
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>SerialNo of OldTicket</td>
     * <td>SerialNO of NewTicket</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>SN-OLD</td>
     * <td>SN-NEW</td>
     * <td>confirmed</td>
     * </tr>
     * </table>
     * <p>
     * If POS issue a reversal request instead of 'payout confirm', then after reversal:
     * <table border="1">
     * <tr>
     * <td>TicketID</td>
     * <td>Ticket SerialNo</td>
     * <td>Ticket Status</td>
     * <td>Is Count In Pool</td>
     * <td>Transaction Type</td>
     * <td>Game Instance ID</td>
     * <td>GameInstance Status</td>
     * </tr>
     * <tr>
     * <td>t1</td>
     * <td>SN-OLD</td>
     * <td>paid</td>
     * <td>true</td>
     * <td>200</td>
     * <td>d1</td>
     * <td>payout started</td>
     * </tr>
     * <tr>
     * <td>t2</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>200</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t3</td>
     * <td>SN-OLD</td>
     * <td>accepted</td>
     * <td>true</td>
     * <td>200</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * <tr>
     * <td>t4</td>
     * <td>SN-NEW</td>
     * <td>invalid</td>
     * <td>false</td>
     * <td>302</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t5</td>
     * <td>SN-NEW</td>
     * <td>invalid</td>
     * <td>false</td>
     * <td>302</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * </table>
     * And the log of new printed ticket will be:
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>SerialNo of OldTicket</td>
     * <td>SerialNO of NewTicket</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>SN-OLD</td>
     * <td>SN-NEW</td>
     * <td>Reversed</td>
     * </tr>
     * </table>
     * And the payout entity will be marked as 'reversed' too:
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>Ticket SN</td>
     * <td>Game Instance</td>
     * <td>Type</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>P1</td>
     * <td>SN-OLD</td>
     * <td>d1</td>
     * <td>Winning</td>
     * <td>Reversed</td>
     * </tr>
     * </table>
     * If it is a single draw ticket, 'payout confirm' can be simply ignored.
     * <p>
     * <h1>Refund Mode</h1>
     * After payout, the ticket's status will be:
     * <table border="1">
     * <tr>
     * <td>TicketID</td>
     * <td>Ticket SerialNo</td>
     * <td>Ticket Status</td>
     * <td>Is Count In Pool</td>
     * <td>Game Instance ID</td>
     * <td>GameInstance Status</td>
     * </tr>
     * <tr>
     * <td>t1</td>
     * <td>SN-OLD</td>
     * <td>paid</td>
     * <td>true</td>
     * <td>d1</td>
     * <td>payout started</td>
     * </tr>
     * <tr>
     * <td>t2</td>
     * <td>SN-OLD</td>
     * <td>returned</td>
     * <td>false</td>
     * <td>d2</td>
     * <td>active</td>
     * </tr>
     * <tr>
     * <td>t3</td>
     * <td>SN-OLD</td>
     * <td>returned</td>
     * <td>false</td>
     * <td>d3</td>
     * <td>new</td>
     * </tr>
     * </table>
     * And 1 payout entities will be generated(won't generate payout for 'returned' tickets):
     * <table border="1">
     * <tr>
     * <td>ID</td>
     * <td>Ticket SN</td>
     * <td>Game Instance</td>
     * <td>Type</td>
     * <td>Status</td>
     * </tr>
     * <tr>
     * <td>P1</td>
     * <td>SN-OLD</td>
     * <td>d1</td>
     * <td>Winning</td>
     * <td>Paid</td>
     * </tr>
     * </table>
     * In this case, 'payout confirmation' will be ignored.
     * 
     * <p>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>The sold draw must be 'payout started'</li>
     * <li>The status of ticket(ticket record associate with sold draw) must be 'accepted'.</li>
     * <li>Current time doesn't exceed last payout claim time.</li>
     * <li>The PIN must be matched if client needs to input PIN.</li>
     * </ul>
     * <p>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Log the payout transaction.</li>
     * <li>Update the ticket record of 'payout started' draw as 'paid'</li>
     * <li>Generate 'NEW_PRIZE_TICKET' record(unconfirmed) if payout mode is 'print new ticket'.</li>
     * <li>Generate 'TE_TICKET' from original winning ticket with a different serialNo, and status is 'accepted') if
     * payout mode is 'print new ticket'.</li>
     * <li>Generate 'PAYOUT' for each winning ticket record(type is winning, and only cash prize will be counted in
     * prize amount and tax amount, etc)</li>
     * <li>generate a return 'PAYOUT' if there are some unclaimed game instance and payout mode is 'refund'.</li>
     * <li>Generate 'PAYOUT_DETAIL' for both cash and object prize.</li>
     * <li>Retore merchant's credit level by actual amount.</li>
     * </ul>
     * <p>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by signed in user.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * </ul>
     * 
     * @param respCtx
     *            The context of transaction.
     * @param clientTicket
     *            The payout request. The following components of Ticket argument must not be null: serialNo,PIN(if the
     *            backend ask to verify PIN), inputChannel.
     * @return the payout result.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    PrizeDto payout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException;

    /**
     * Why we need payout confirmation??? The main goal of this transaction is to guarantee that all sold tickets will
     * join winning analysis. If player buy a multiple-draw ticket, and this ticket win a prize. when player go to
     * merchant to claim prize, merchant will do payout for all finished game draw, and do refund for all future game
     * draw. If payout mode is 'print new ticket', there is possibility that 'conform' request can not reach TE. Does
     * how TE handle this situation? If no confirm request reaches TE, client maybe encounter below 2 situations:
     * <ul>
     * <li>A: client has printed new ticket, and paid cash to player</li>
     * <li>B: client didn't print new ticket, returned old physical ticket to player.</li>
     * </ul>
     * At TE, due to no confirm request received, both old tickets and new printed tickets are valid, they will both
     * join winning analysis. Ultimately, either old or new printed tickets should be think as company absorption. To
     * situation A, player has got prize and new printed ticket, he/she just thought the payout has finished
     * successfully. Later he/she will maybe come back to claim prize with new printed tickets. So in this case, TE
     * should think old tickets as company absorption. To situation B, player didn't got prize, he/she will get back the
     * old tickets, so TE should think new printed tickets as company absorption.
     * 
     * @see #payout(Context, BaseTicket)
     */
    void confirmPayout(Context<?> respCtx, BaseTicket ticket) throws ApplicationException;
}
