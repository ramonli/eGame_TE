package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CashoutPass.class)
public abstract class CashoutPass_ {

	public static volatile SingularAttribute<CashoutPass, String> updateBy;
	public static volatile SingularAttribute<CashoutPass, Date> expireTime;
	public static volatile SingularAttribute<CashoutPass, BigDecimal> cashoutAmount;
	public static volatile SingularAttribute<CashoutPass, Date> createTime;
	public static volatile SingularAttribute<CashoutPass, String> operatorId;
	public static volatile SingularAttribute<CashoutPass, Date> updateTime;
	public static volatile SingularAttribute<CashoutPass, Integer> triedTimes;
	public static volatile SingularAttribute<CashoutPass, String> teTransactionId;
	public static volatile SingularAttribute<CashoutPass, String> createBy;
	public static volatile SingularAttribute<CashoutPass, String> cashoutPassword;
	public static volatile SingularAttribute<CashoutPass, String> id;
	public static volatile SingularAttribute<CashoutPass, String> cashoutTeTransactionId;
	public static volatile SingularAttribute<CashoutPass, String> cashoutBarCode;

}

