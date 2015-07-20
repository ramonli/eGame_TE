package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LuckyWinningItem.class)
public abstract class LuckyWinningItem_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<LuckyWinningItem, Boolean> valid;
	public static volatile SingularAttribute<LuckyWinningItem, Integer> numberOfLevel;
	public static volatile SingularAttribute<LuckyWinningItem, String> ticketSerialNo;
	public static volatile SingularAttribute<LuckyWinningItem, String> gameInstanceId;
	public static volatile SingularAttribute<LuckyWinningItem, Integer> prizeLevel;

}

