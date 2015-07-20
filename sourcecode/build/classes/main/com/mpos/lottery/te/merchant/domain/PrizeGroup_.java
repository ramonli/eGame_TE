package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PrizeGroup.class)
public abstract class PrizeGroup_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<PrizeGroup, BigDecimal> maxPayoutAmount;
	public static volatile SingularAttribute<PrizeGroup, BigDecimal> dailyCashoutLimit;
	public static volatile SingularAttribute<PrizeGroup, Integer> allowType;
	public static volatile SingularAttribute<PrizeGroup, BigDecimal> minPayoutAmount;
	public static volatile SingularAttribute<PrizeGroup, Boolean> isPayoutAllowed;

}

