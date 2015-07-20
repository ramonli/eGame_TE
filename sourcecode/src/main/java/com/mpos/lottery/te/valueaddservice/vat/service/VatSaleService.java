package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.support.VatReversalOrCancelStrategy;

public interface VatSaleService {

    /**
     * VAT amount can be used to buy raffle or magic100 ticket. Raffle ticket is for B2B retailer, and magic100 ticket
     * is for B2C retailer.
     * <p>
     * Though this service is extending from <code>TicketService</code>, its logic is completedly different from sale
     * service of a real game, actually it is more understandable if regard this service as a front end of
     * Raffle/Magic100 sale service, and the implementation of this service will definitely dispatch request to
     * Raffle(B2B)/Magic100(B2C) sale service accordingly.
     * <p>
     * Before dispatching request, this service will perform below operations:
     * <ul>
     * <li>Determine the business type of device. B2B or B2C??</li>
     * <li>
     * <b>if B2B</b>
     * <ol>
     * <li>No matter the VAT amount, a single raffle ticket will be sold.</li>
     * <li>Record the VAT sale transaction <code>VatSaleTransaction</code>.</li>
     * <li>Increment Vat sale balance.</code>
     * </ol>
     * </li>
     * <li>
     * <b>If B2C</b>
     * <ol>
     * <li>Determine the total amount of sale by (vatAmount*vatRate), also round down/up will be considered. For example
     * vatAmount is 800, vatRate is 0.3, the sale total amount will be <code>800*0.3=240</code>, if round down, and base
     * amount of magic100 is 100, then only 2 tickets can be sold(240/100=2.4, round down to 2).</li>
     * <li>Record the VAT sale transaction <code>VatSaleTransaction</code>.</li>
     * <li>Increment Vat sale balance.</code></li>
     * </ol>
     * </li>
     * </ul>
     * <p/>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>VAT is valid.</li>
     * <li>VAT has been allocated to current merchant</li>
     * </ul>
     * <p>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Record the general
     * <code>Transaction<code>, in which the game type will be set to Raffle or Magic100 accordingly, 
     * also the total amount will be set to SALE total amount.</li>
     * <li>Record the detailed <code>VatSaleTransaction</code>.</li>
     * <li>Increment the VAT sale balance</code>
     * <li>Generate tickets which have been sold.</li>
     * </ul>
     * <p>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by authorized GPEs.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * </ul>
     * 
     * @param respCtx
     *            The context represents the response.
     * @param clientSale
     *            A client ticket request, a instance of <code>VatTicket</code>.
     * @return The sold ticket, either RAFFLE or MAGIC100.
     */
    public VatSaleTransaction sell(Context<?> respCtx, VatSaleTransaction clientSale) throws ApplicationException;

    /**
     * Refund a VAT invoice. Customer can request to refund sales and the associated VAT invoice must be refunded as
     * well, in essential it is same with the cancellation of {@link #sell(Context, VatSaleTransaction)}.
     * <p>
     * Refer to
     * {@link VatReversalOrCancelStrategy#cancelOrReverse(Context, com.mpos.lottery.te.trans.domain.Transaction)}
     * 
     * @param respCtx
     *            The context of current refunding transaction.
     * @param vatTrans
     *            The refunding request, the following components must be set: vatRefNo
     * @throws ApplicationException
     *             if encounter any biz exception.
     */
    void refundVat(Context<?> respCtx, VatSaleTransaction vatTrans) throws ApplicationException;
}
