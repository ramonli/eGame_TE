package com.mpos.lottery.te.trans.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TransactionRetryLog.class)
public abstract class TransactionRetryLog_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<TransactionRetryLog, Integer> transType;
	public static volatile SingularAttribute<TransactionRetryLog, String> ticketSerialNo;
	public static volatile SingularAttribute<TransactionRetryLog, Integer> totalRetry;
	public static volatile SingularAttribute<TransactionRetryLog, Long> deviceId;

}

