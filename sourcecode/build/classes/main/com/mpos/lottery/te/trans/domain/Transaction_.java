package com.mpos.lottery.te.trans.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Transaction.class)
public abstract class Transaction_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<Transaction, Integer> cancelTransactionType;
	public static volatile SingularAttribute<Transaction, Integer> responseCode;
	public static volatile SingularAttribute<Transaction, String> operatorId;
	public static volatile SingularAttribute<Transaction, String> cancelTransactionId;
	public static volatile SingularAttribute<Transaction, String> destinationOpeator;
	public static volatile SingularAttribute<Transaction, String> ticketSerialNo;
	public static volatile SingularAttribute<Transaction, String> gameId;
	public static volatile SingularAttribute<Transaction, Integer> type;
	public static volatile SingularAttribute<Transaction, Date> transTimestamp;
	public static volatile SingularAttribute<Transaction, String> traceMessageId;
	public static volatile SingularAttribute<Transaction, String> virn;
	public static volatile SingularAttribute<Transaction, String> batchNumber;
	public static volatile SingularAttribute<Transaction, BigDecimal> totalAmount;
	public static volatile SingularAttribute<Transaction, String> parentMerchants;
	public static volatile SingularAttribute<Transaction, Long> merchantId;
	public static volatile SingularAttribute<Transaction, BigDecimal> longitude;
	public static volatile SingularAttribute<Transaction, BigDecimal> latitude;
	public static volatile SingularAttribute<Transaction, String> gpeId;
	public static volatile SingularAttribute<Transaction, Long> deviceId;

}

