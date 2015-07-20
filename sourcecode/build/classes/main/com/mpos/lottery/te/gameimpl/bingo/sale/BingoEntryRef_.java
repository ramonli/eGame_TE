package com.mpos.lottery.te.gameimpl.bingo.sale;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BingoEntryRef.class)
public abstract class BingoEntryRef_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<BingoEntryRef, Integer> status;
	public static volatile SingularAttribute<BingoEntryRef, String> selectedNumber;
	public static volatile SingularAttribute<BingoEntryRef, String> gameInstanceId;

}

