package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BalanceTransactions.class)
public abstract class BalanceTransactions_ {

	public static volatile SingularAttribute<BalanceTransactions, Timestamp> createTime;
	public static volatile SingularAttribute<BalanceTransactions, BigDecimal> transactionAmount;
	public static volatile SingularAttribute<BalanceTransactions, String> operatorId;
	public static volatile SingularAttribute<BalanceTransactions, Timestamp> updateTime;
	public static volatile SingularAttribute<BalanceTransactions, Integer> status;
	public static volatile SingularAttribute<BalanceTransactions, String> ownerId;
	public static volatile SingularAttribute<BalanceTransactions, Integer> paymentType;
	public static volatile SingularAttribute<BalanceTransactions, Integer> ownerType;
	public static volatile SingularAttribute<BalanceTransactions, String> gameId;
	public static volatile SingularAttribute<BalanceTransactions, String> teTransactionId;
	public static volatile SingularAttribute<BalanceTransactions, BigDecimal> commissionAmount;
	public static volatile SingularAttribute<BalanceTransactions, Long> toParentMerchantId;
	public static volatile SingularAttribute<BalanceTransactions, Long> id;
	public static volatile SingularAttribute<BalanceTransactions, BigDecimal> commissionRate;
	public static volatile SingularAttribute<BalanceTransactions, Integer> transactionType;
	public static volatile SingularAttribute<BalanceTransactions, Long> merchantId;
	public static volatile SingularAttribute<BalanceTransactions, Long> fromParentMerchantId;
	public static volatile SingularAttribute<BalanceTransactions, Integer> originalTransType;
	public static volatile SingularAttribute<BalanceTransactions, Long> deviceId;

}

