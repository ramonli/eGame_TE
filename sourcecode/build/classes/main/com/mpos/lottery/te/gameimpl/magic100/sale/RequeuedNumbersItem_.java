package com.mpos.lottery.te.gameimpl.magic100.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RequeuedNumbersItem.class)
public abstract class RequeuedNumbersItem_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<RequeuedNumbersItem, Long> sequenceOfNumber;
	public static volatile SingularAttribute<RequeuedNumbersItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<RequeuedNumbersItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<RequeuedNumbersItem, Integer> state;
	public static volatile SingularAttribute<RequeuedNumbersItem, String> luckyNumber;
	public static volatile SingularAttribute<RequeuedNumbersItem, RequeuedNumbers> requeuedNumbers;

}

