package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PrizeLevelItem.class)
public abstract class PrizeLevelItem_ {

	public static volatile SingularAttribute<PrizeLevelItem, String> id;
	public static volatile SingularAttribute<PrizeLevelItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<PrizeLevelItem, String> objectName;
	public static volatile SingularAttribute<PrizeLevelItem, String> objectId;
	public static volatile SingularAttribute<PrizeLevelItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<PrizeLevelItem, PrizeLevel> prizeLevel;
	public static volatile SingularAttribute<PrizeLevelItem, Integer> numberOfObject;
	public static volatile SingularAttribute<PrizeLevelItem, Integer> prizeType;
	public static volatile SingularAttribute<PrizeLevelItem, BigDecimal> actualAmount;

}

