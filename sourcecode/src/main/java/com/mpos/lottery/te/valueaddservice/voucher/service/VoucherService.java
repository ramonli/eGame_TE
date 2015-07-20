package com.mpos.lottery.te.valueaddservice.voucher.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;

public interface VoucherService {

    /**
     * This interface will lookup a valid voucher and return to client. A valid voucher must meet below limits,
     * <ol>
     * <li>the face amount must be same with user's request</li>
     * <li>must not pass the expire date(for example, can't sell a voucher which will expire in one week)</li>
     * <li>voucher can't be sold more than once.</li>
     * </ol>
     * <p/>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>game must has be allocated to merchant</li>
     * <li>operator/merchant has enough sale balance</li>
     * </ul>
     * <p/>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Generate record of <code>VoucherSale</code>.</li>
     * <li>Mark sold voucher as 'sold' status.</li>
     * <li>calculation commission of operator.</li>
     * <li>publish transaction message to RabbitMQ.</li>
     * </ul>
     * <p/>
     * <b>Usage-Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by authorized GPEs.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * </ul>
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param reqDto
     *            The DTO of voucher sale request.
     * @return A valid voucher instance.
     * @throws ApplicationException
     *             if encounter a business exception.
     */
    Voucher sell(Context respCtx, Voucher reqDto) throws ApplicationException;
}
