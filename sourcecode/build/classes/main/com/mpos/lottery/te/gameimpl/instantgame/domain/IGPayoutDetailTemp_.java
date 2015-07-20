package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(IGPayoutDetailTemp.class)
public abstract class IGPayoutDetailTemp_ {

	public static volatile SingularAttribute<IGPayoutDetailTemp, String> updateBy;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Date> createTime;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> operatorId;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Integer> topupMode;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Date> updateTime;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> objectName;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> payoutId;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> createBy;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> id;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Long> iGBatchNumber;
	public static volatile SingularAttribute<IGPayoutDetailTemp, BigDecimal> topupAmount;
	public static volatile SingularAttribute<IGPayoutDetailTemp, String> objectId;
	public static volatile SingularAttribute<IGPayoutDetailTemp, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Integer> numberOfObject;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Integer> objectType;
	public static volatile SingularAttribute<IGPayoutDetailTemp, BigDecimal> actualAmount;
	public static volatile SingularAttribute<IGPayoutDetailTemp, Integer> payoutType;

}

