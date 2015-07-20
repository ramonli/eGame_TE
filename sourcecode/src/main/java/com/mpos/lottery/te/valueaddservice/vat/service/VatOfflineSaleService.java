package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.web.VatOfflineSaleUploadDto;

public interface VatOfflineSaleService {

    /**
     * Sync the offline VAT sale with the backend. Client will upload offline VAT sale in batch mode, and the backend
     * will sync them one by one.
     * <p>
     * <h1>Fail to sync a VAT sale</h1>
     * Fail to sync a single VAT sale won't affect the handling of other VAT sales, that says the backend will continue
     * to handle next offline VAT sale, and simply ignore the failed one, however the failed transctions will be
     * returned to client.
     * <h1>Upload after winner analysis</h1> In offline mode, we can't guarantee that all sales can be uploaded before
     * winner analysis. For example retailer sells a ticket a Draw#1, however due to some reasons, s/he can only upload
     * those offline sales when Draw#2, how does the backend handle this case? Lets take a look of below example.
     * <table border="1">
     * <tr>
     * <td>TicketSerialNo</td>
     * <td>GameInstance</td>
     * </tr>
     * <tr>
     * <td>S1</td>
     * <td>D1</td>
     * </tr>
     * <tr>
     * <td>S2</td>
     * <td>D2</td>
     * </tr>
     * </table>
     * <p>
     * Now the active game instance is D3(both D1 and D2 are payout-started), and retailer start to upload those offline
     * VAT sales, in this case S1 will still join winner analysis of D1(the winner analysis will be performed by
     * M.Lottery, TE will send a message to M.Lottery), and follows the same rule, ticket S2 will join winner analysis
     * of D2.
     * <p>
     * However the backend will count the total amount of all tickets(both Raffle and Magic100) of those offline VAT
     * sales to a special turnover of current active game instance D3, this is just for reference.
     * <p>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>None of any associated game instances is 'in progress of winner analysis'. if we do uploading during winner
     * analysis, it will break the process of winner analysis.</li>
     * </ul>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Generate {@link TransactionType#VAT_UPLOAD_OFFLINESALE} transaction.</li>
     * <li>Generate VAT sale transaction accordingly if matched.</li>
     * <li>Generate {@link com.mpos.lottery.te.gamespec.sale.OfflineTicketLog} if needed, and one record per game
     * instance.</li>
     * <li>Generate Raffle or Magic100 ticket if needed.</li>
     * </ul>
     * <p>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by signed in user.</li>
     * <li>Count of Transaction - There is a limitation on count of transaction in a single request, default is 100.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * <li>Message Redelivery - Client can redeliver the same message content(with different trace message ID) any
     * number of times, the backend will guarantee data consistency.</li>
     * </ul>
     * 
     * @param respCtx
     *            THe context of current transaction.
     * @param uploadDto
     *            THe request DTO which carries offline VAT sale transactions.
     * @return only those unmatched/failed vat transactions.
     * @throws ApplicationException
     *             if encounter any biz exception.
     */
    VatOfflineSaleUploadDto upload(Context respCtx, VatOfflineSaleUploadDto uploadDto) throws ApplicationException;

}
