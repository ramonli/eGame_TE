package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategy;

/**
 * Composite all sale related interfaces.
 * 
 * @author Ramon
 * 
 */
public interface CompositeTicketService extends TicketService, ReversalOrCancelStrategy, TicketEnquiryService {

}
