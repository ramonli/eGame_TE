package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseWinningStatistics.class)
public abstract class BaseWinningStatistics_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<BaseWinningStatistics, BigDecimal> taxAmount;
	public static volatile SingularAttribute<BaseWinningStatistics, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<BaseWinningStatistics, Integer> numberOfPrize;
	public static volatile SingularAttribute<BaseWinningStatistics, String> gameInstanceId;
	public static volatile SingularAttribute<BaseWinningStatistics, Integer> prizeLevel;
	public static volatile SingularAttribute<BaseWinningStatistics, BigDecimal> actualAmount;

}

