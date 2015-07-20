package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PayoutDetail.class)
public abstract class PayoutDetail_ {

	public static volatile SingularAttribute<PayoutDetail, String> updateBy;
	public static volatile SingularAttribute<PayoutDetail, Date> createTime;
	public static volatile SingularAttribute<PayoutDetail, Integer> topupMode;
	public static volatile SingularAttribute<PayoutDetail, Date> updateTime;
	public static volatile SingularAttribute<PayoutDetail, String> objectName;
	public static volatile SingularAttribute<PayoutDetail, String> payoutId;
	public static volatile SingularAttribute<PayoutDetail, String> createBy;
	public static volatile SingularAttribute<PayoutDetail, String> id;
	public static volatile SingularAttribute<PayoutDetail, BigDecimal> topupAmount;
	public static volatile SingularAttribute<PayoutDetail, String> objectId;
	public static volatile SingularAttribute<PayoutDetail, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<PayoutDetail, Integer> numberOfObject;
	public static volatile SingularAttribute<PayoutDetail, Integer> objectType;
	public static volatile SingularAttribute<PayoutDetail, BigDecimal> actualAmount;
	public static volatile SingularAttribute<PayoutDetail, Integer> payoutType;

}

