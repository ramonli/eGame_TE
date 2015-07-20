package com.mpos.lottery.te.gamespec.prize.support.luckydraw;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LuckyDrawWinningItem.class)
public abstract class LuckyDrawWinningItem_ extends com.mpos.lottery.te.gamespec.prize.BaseWinningItem_ {

	public static volatile SingularAttribute<LuckyDrawWinningItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<LuckyDrawWinningItem, Integer> status;
	public static volatile SingularAttribute<LuckyDrawWinningItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<LuckyDrawWinningItem, BigDecimal> actualAmount;

}

