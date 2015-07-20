package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Merchant.class)
public abstract class Merchant_ {

	public static volatile SingularAttribute<Merchant, Integer> incentiveTarget;
	public static volatile SingularAttribute<Merchant, Boolean> deductSaleByCreditCard;
	public static volatile SingularAttribute<Merchant, Long> id;
	public static volatile SingularAttribute<Merchant, String> taxNo;
	public static volatile SingularAttribute<Merchant, BigDecimal> saleCreditLevel;
	public static volatile SingularAttribute<Merchant, BigDecimal> dailyCashoutLevel;
	public static volatile SingularAttribute<Merchant, String> name;
	public static volatile SingularAttribute<Merchant, String> parentMerchants;
	public static volatile SingularAttribute<Merchant, Integer> maxMultipleBets;
	public static volatile SingularAttribute<Merchant, Boolean> distributor;
	public static volatile SingularAttribute<Merchant, Integer> allowedMultiDraw;
	public static volatile SingularAttribute<Merchant, BigDecimal> cashoutRate;
	public static volatile SingularAttribute<Merchant, Timestamp> updateTime;
	public static volatile SingularAttribute<Merchant, Integer> status;
	public static volatile SingularAttribute<Merchant, BigDecimal> topupReat;
	public static volatile SingularAttribute<Merchant, PrizeGroup> cashoutGroup;
	public static volatile SingularAttribute<Merchant, Boolean> salePayoutUnderSameDistributor;
	public static volatile SingularAttribute<Merchant, BigDecimal> cashoutBalance;
	public static volatile SingularAttribute<Merchant, PrizeGroup> prizeGroup;
	public static volatile SingularAttribute<Merchant, String> code;
	public static volatile SingularAttribute<Merchant, BigDecimal> payoutCreditLevel;
	public static volatile SingularAttribute<Merchant, Integer> creditType;
	public static volatile SingularAttribute<Merchant, Merchant> parentMerchant;
	public static volatile SingularAttribute<Merchant, Long> maxOfflineTickets;
	public static volatile SingularAttribute<Merchant, BigDecimal> commisionBalance;

}

