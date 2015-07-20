package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BasePrizeObject.class)
public abstract class BasePrizeObject_ {

	public static volatile SingularAttribute<BasePrizeObject, String> id;
	public static volatile SingularAttribute<BasePrizeObject, BigDecimal> price;
	public static volatile SingularAttribute<BasePrizeObject, BigDecimal> tax;
	public static volatile SingularAttribute<BasePrizeObject, String> name;
	public static volatile SingularAttribute<BasePrizeObject, Integer> type;

}

