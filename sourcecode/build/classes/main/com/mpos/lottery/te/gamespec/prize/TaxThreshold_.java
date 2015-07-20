package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TaxThreshold.class)
public abstract class TaxThreshold_ {

	public static volatile SingularAttribute<TaxThreshold, String> id;
	public static volatile SingularAttribute<TaxThreshold, Integer> ruleType;
	public static volatile SingularAttribute<TaxThreshold, BigDecimal> taxAmount;
	public static volatile SingularAttribute<TaxThreshold, BigDecimal> minAmount;
	public static volatile SingularAttribute<TaxThreshold, String> taxPolicyId;
	public static volatile SingularAttribute<TaxThreshold, Integer> taxBase;
	public static volatile SingularAttribute<TaxThreshold, BigDecimal> maxAmount;
	public static volatile SingularAttribute<TaxThreshold, TaxDateRange> taxDateRange;

}

