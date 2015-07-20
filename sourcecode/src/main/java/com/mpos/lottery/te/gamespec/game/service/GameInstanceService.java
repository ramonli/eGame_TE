package com.mpos.lottery.te.gamespec.game.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

/**
 * Each game type should provide a dedicated implementation.
 * 
 * @author Ramon Li
 */
public interface GameInstanceService {

    /**
     * Lookup all active and sale-ready game instances of given <code>gameType</code> which have been allocated to given
     * merchant.
     */
    List<? extends BaseGameInstance> enquirySaleReady(Context response) throws ApplicationException;

    /**
     * Lookup active and sale-ready game instance of given game which has been allocated to given merchant. In general
     * there should be only one game instance returned.
     */
    List<? extends BaseGameInstance> enquirySaleReady(Context response, String gameId) throws ApplicationException;

    /**
     * Lookup game instance by given <code>gameId</code> and <code>number</code> which has been allocated to given
     * merchant.
     */
    BaseGameInstance enquiry(Context respCtx, String gameId, String number) throws ApplicationException;

    /**
     * Lookup sequential <code>multipleDraws</code> ready-for-sale game instances from the given one which is identified
     * by <code>gameID</code> and <code>number</code> which has been allocated to given merchant. The first game
     * instance must be active, that is ready-for-sale.
     * 
     * @param gameId
     *            The identifier of game of requested game instance.
     * @param number
     *            The number of game instance.
     * @param multipleDraws
     *            How many game instances required?
     */
    List<? extends BaseGameInstance> enquirySaleReady(Context response, String gameId, String number, int multipleDraws)
            throws ApplicationException;

    /**
     * Lookup sequential <code>multipleDraws</code> game instances since the game instance which identified by
     * <code>gameId</code> and <code>number</code>.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param gameId
     *            The identifier of game.
     * @param number
     *            The number of requested game instance.
     * @param multipleDraws
     *            How many game instances does the client request?
     * @return The requested game instances.
     * @throws ApplicationException
     *             if encounter any biz exception.
     */
    List<? extends BaseGameInstance> enquiryMultiDraw(Context respCtx, String gameId, String number, int multipleDraws)
            throws ApplicationException;

    /**
     * Whether the game instances associate with ticket are ready for payout.
     */
    void allowPayout(Context respCtx, List<? extends BaseTicket> hostTickets, boolean isEnquiry)
            throws ApplicationException;

}
