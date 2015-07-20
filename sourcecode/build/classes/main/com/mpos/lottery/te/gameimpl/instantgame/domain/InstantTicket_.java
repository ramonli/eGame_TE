package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(InstantTicket.class)
public abstract class InstantTicket_ {

	public static volatile SingularAttribute<InstantTicket, Boolean> validationSettled;
	public static volatile SingularAttribute<InstantTicket, String> operatorId;
	public static volatile SingularAttribute<InstantTicket, Boolean> isSoldToCustomer;
	public static volatile SingularAttribute<InstantTicket, String> bookNumber;
	public static volatile SingularAttribute<InstantTicket, String> id;
	public static volatile SingularAttribute<InstantTicket, Integer> physicalStatus;
	public static volatile SingularAttribute<InstantTicket, Boolean> suspendValidation;
	public static volatile SingularAttribute<InstantTicket, String> saleTransId;
	public static volatile SingularAttribute<InstantTicket, Boolean> suspendActivation;
	public static volatile SingularAttribute<InstantTicket, String> merchantId;
	public static volatile SingularAttribute<InstantTicket, String> ticketXOR1;
	public static volatile SingularAttribute<InstantTicket, String> ticketXOR2;
	public static volatile SingularAttribute<InstantTicket, Integer> status;
	public static volatile SingularAttribute<InstantTicket, String> ticketXOR3;
	public static volatile SingularAttribute<InstantTicket, String> ticketMAC;
	public static volatile SingularAttribute<InstantTicket, Integer> prizeLevelIndex;
	public static volatile SingularAttribute<InstantTicket, String> serialNo;
	public static volatile SingularAttribute<InstantTicket, BigDecimal> taxAmount;
	public static volatile SingularAttribute<InstantTicket, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<InstantTicket, InstantGameDraw> gameDraw;
	public static volatile SingularAttribute<InstantTicket, Date> soldTime;
	public static volatile SingularAttribute<InstantTicket, Integer> prizeLevel;
	public static volatile SingularAttribute<InstantTicket, String> devId;
	public static volatile SingularAttribute<InstantTicket, Boolean> isInBlacklist;
	public static volatile SingularAttribute<InstantTicket, String> xorMD5;

}

