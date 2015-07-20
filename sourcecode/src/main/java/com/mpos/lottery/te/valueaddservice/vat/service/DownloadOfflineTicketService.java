package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.SelectedNumberPackDto;

/**
 * @author terry
 * @version [3.3.1, 2014-7-25]
 */
public interface DownloadOfflineTicketService {

    /**
     * Get set aside for magic100 game numbers, how many numbers are based on the credit belongs to distribute to set.
     * <p>
     */
    SelectedNumberPackDto getReservedNumbers(Context respCtx, String magic100GameInstanceId, String gameId,
            long maxNumberSeq, SelectedNumberPackDto dto) throws ApplicationException;

    OfflineTicketPackDto downloadOfflineTicket(Context respCtx, OfflineTicketPackDto offlineTicketPackDto)
            throws ApplicationException;

}
