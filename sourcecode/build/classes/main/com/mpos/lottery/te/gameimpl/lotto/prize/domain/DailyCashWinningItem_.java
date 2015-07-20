package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(DailyCashWinningItem.class)
public abstract class DailyCashWinningItem_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<DailyCashWinningItem, Boolean> valid;
	public static volatile SingularAttribute<DailyCashWinningItem, String> ticketSerialNo;
	public static volatile SingularAttribute<DailyCashWinningItem, String> prizeLogicId;
	public static volatile SingularAttribute<DailyCashWinningItem, String> prizeLevelId;
	public static volatile SingularAttribute<DailyCashWinningItem, Integer> numberOfPrize;
	public static volatile SingularAttribute<DailyCashWinningItem, String> gameInstanceId;
	public static volatile SingularAttribute<DailyCashWinningItem, Integer> prizeLevel;

}

