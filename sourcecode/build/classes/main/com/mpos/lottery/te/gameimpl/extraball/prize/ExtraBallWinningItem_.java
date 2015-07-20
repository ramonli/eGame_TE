package com.mpos.lottery.te.gameimpl.extraball.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ExtraBallWinningItem.class)
public abstract class ExtraBallWinningItem_ extends com.mpos.lottery.te.gamespec.prize.BaseWinningItem_ {

	public static volatile SingularAttribute<ExtraBallWinningItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<ExtraBallWinningItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<ExtraBallWinningItem, BigDecimal> actualAmount;

}

