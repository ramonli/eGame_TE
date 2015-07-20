package com.mpos.lottery.te.gamespec.prize;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseWinningItem.class)
public abstract class BaseWinningItem_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<BaseWinningItem, Boolean> valid;
	public static volatile SingularAttribute<BaseWinningItem, String> ticketSerialNo;
	public static volatile SingularAttribute<BaseWinningItem, Integer> numberOfPrize;
	public static volatile SingularAttribute<BaseWinningItem, String> gameInstanceId;
	public static volatile SingularAttribute<BaseWinningItem, Integer> prizeLevel;
	public static volatile SingularAttribute<BaseWinningItem, String> entryId;

}

