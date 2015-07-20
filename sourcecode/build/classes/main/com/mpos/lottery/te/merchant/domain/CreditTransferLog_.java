package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CreditTransferLog.class)
public abstract class CreditTransferLog_ {

	public static volatile SingularAttribute<CreditTransferLog, Date> createTime;
	public static volatile SingularAttribute<CreditTransferLog, String> toMerchantName;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> payoutCreditOfTransfer;
	public static volatile SingularAttribute<CreditTransferLog, String> transactionId;
	public static volatile SingularAttribute<CreditTransferLog, Integer> status;
	public static volatile SingularAttribute<CreditTransferLog, Date> updateTime;
	public static volatile SingularAttribute<CreditTransferLog, String> toMerchantCode;
	public static volatile SingularAttribute<CreditTransferLog, Integer> transferType;
	public static volatile SingularAttribute<CreditTransferLog, Long> toMerchantId;
	public static volatile SingularAttribute<CreditTransferLog, Long> fromMerchantId;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> saleCreditOfFromOperatorAfter;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> payoutCreditOfToOperatorAfter;
	public static volatile SingularAttribute<CreditTransferLog, String> fromOperatorId;
	public static volatile SingularAttribute<CreditTransferLog, String> id;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> payoutCreditOfFromOperatorAfter;
	public static volatile SingularAttribute<CreditTransferLog, String> toOperatorId;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> saleCreditOfToOperatorAfter;
	public static volatile SingularAttribute<CreditTransferLog, BigDecimal> saleCreditOfTransfer;
	public static volatile SingularAttribute<CreditTransferLog, Integer> targetType;
	public static volatile SingularAttribute<CreditTransferLog, Long> deviceId;

}

