package com.mpos.lottery.te.gameimpl.magic100.sale;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RequeuedNumbers.class)
public abstract class RequeuedNumbers_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<RequeuedNumbers, String> transactionId;
	public static volatile SingularAttribute<RequeuedNumbers, Long> beginOfValidNumbers;
	public static volatile SingularAttribute<RequeuedNumbers, Integer> countOfValidNumbers;
	public static volatile ListAttribute<RequeuedNumbers, RequeuedNumbersItem> requeuedNumbersItemList;
	public static volatile SingularAttribute<RequeuedNumbers, String> gameInstanceId;
	public static volatile SingularAttribute<RequeuedNumbers, Integer> countOfNumbers;

}

