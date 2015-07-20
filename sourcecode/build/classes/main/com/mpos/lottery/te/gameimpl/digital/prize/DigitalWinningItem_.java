package com.mpos.lottery.te.gameimpl.digital.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(DigitalWinningItem.class)
public abstract class DigitalWinningItem_ extends com.mpos.lottery.te.gamespec.prize.BaseWinningItem_ {

	public static volatile SingularAttribute<DigitalWinningItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<DigitalWinningItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<DigitalWinningItem, BigDecimal> actualAmount;

}

