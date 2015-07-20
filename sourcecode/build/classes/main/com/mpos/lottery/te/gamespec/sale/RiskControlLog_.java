package com.mpos.lottery.te.gamespec.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RiskControlLog.class)
public abstract class RiskControlLog_ {

	public static volatile SingularAttribute<RiskControlLog, String> id;
	public static volatile SingularAttribute<RiskControlLog, String> selectedNumber;
	public static volatile SingularAttribute<RiskControlLog, BigDecimal> totalAmount;
	public static volatile SingularAttribute<RiskControlLog, String> gameInstanceId;
	public static volatile SingularAttribute<RiskControlLog, Integer> prizeLevelType;

}

