package com.mpos.lottery.te.valueaddservice.airtime;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AirtimeParameter.class)
public abstract class AirtimeParameter_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<AirtimeParameter, BigDecimal> minAmount;
	public static volatile SingularAttribute<AirtimeParameter, Integer> airtimeProviderId;
	public static volatile SingularAttribute<AirtimeParameter, BigDecimal> maxAmount;
	public static volatile SingularAttribute<AirtimeParameter, BigDecimal> stepAmount;
	public static volatile SingularAttribute<AirtimeParameter, Boolean> amountStepSupport;

}

