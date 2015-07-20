package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Operator.class)
public abstract class Operator_ {

	public static volatile SingularAttribute<Operator, BigDecimal> cashoutRate;
	public static volatile SingularAttribute<Operator, Timestamp> updateTime;
	public static volatile SingularAttribute<Operator, BigDecimal> topupReat;
	public static volatile SingularAttribute<Operator, Integer> status;
	public static volatile SingularAttribute<Operator, PrizeGroup> cashoutGroup;
	public static volatile SingularAttribute<Operator, BigDecimal> cashoutBalance;
	public static volatile SingularAttribute<Operator, PrizeGroup> prizeGroup;
	public static volatile SingularAttribute<Operator, String> type;
	public static volatile SingularAttribute<Operator, String> password;
	public static volatile SingularAttribute<Operator, String> id;
	public static volatile SingularAttribute<Operator, BigDecimal> saleCreditLevel;
	public static volatile SingularAttribute<Operator, Boolean> ignoreCredit;
	public static volatile SingularAttribute<Operator, BigDecimal> payoutCreditLevel;
	public static volatile SingularAttribute<Operator, Integer> creditType;
	public static volatile SingularAttribute<Operator, BigDecimal> dailyCashoutLevel;
	public static volatile SingularAttribute<Operator, Boolean> needEnrollment;
	public static volatile SingularAttribute<Operator, BigDecimal> commisionBalance;
	public static volatile SingularAttribute<Operator, String> loginName;

}

