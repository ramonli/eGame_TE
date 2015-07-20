package com.mpos.lottery.te.gamespec.sale;

import com.mpos.lottery.te.trans.domain.Transaction;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseTicket.class)
public abstract class BaseTicket_ extends com.mpos.lottery.te.common.dao.SettlementEntity_ {

	public static volatile SingularAttribute<BaseTicket, Boolean> isWinning;
	public static volatile SingularAttribute<BaseTicket, String> validationCode;
	public static volatile SingularAttribute<BaseTicket, Integer> totalBets;
	public static volatile SingularAttribute<BaseTicket, Transaction> transaction;
	public static volatile SingularAttribute<BaseTicket, Integer> status;
	public static volatile SingularAttribute<BaseTicket, String> barcode;
	public static volatile SingularAttribute<BaseTicket, Integer> ticketFrom;
	public static volatile SingularAttribute<BaseTicket, Long> version;
	public static volatile SingularAttribute<BaseTicket, String> serialNo;
	public static volatile SingularAttribute<BaseTicket, Boolean> absorptionPaid;
	public static volatile SingularAttribute<BaseTicket, Integer> transType;
	public static volatile SingularAttribute<BaseTicket, Boolean> isCountInPool;
	public static volatile SingularAttribute<BaseTicket, String> creditCardSN;
	public static volatile SingularAttribute<BaseTicket, Integer> ticketType;
	public static volatile SingularAttribute<BaseTicket, String> userId;
	public static volatile SingularAttribute<BaseTicket, Boolean> isOffline;
	public static volatile SingularAttribute<BaseTicket, BigDecimal> totalAmount;
	public static volatile SingularAttribute<BaseTicket, Boolean> isLuckyWinning;
	public static volatile SingularAttribute<BaseTicket, String> PIN;
	public static volatile SingularAttribute<BaseTicket, Integer> multipleDraws;
	public static volatile SingularAttribute<BaseTicket, Integer> countOfLuckyWinning;
	public static volatile SingularAttribute<BaseTicket, String> mobile;
	public static volatile SingularAttribute<BaseTicket, Boolean> isPayoutBlocked;

}

