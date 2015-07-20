package com.mpos.lottery.te.gameimpl.lfn.prize;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LfnWinningItem.class)
public abstract class LfnWinningItem_ extends com.mpos.lottery.te.gamespec.prize.BaseWinningItem_ {

	public static volatile SingularAttribute<LfnWinningItem, BigDecimal> taxAmount;
	public static volatile SingularAttribute<LfnWinningItem, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<LfnWinningItem, BigDecimal> actualAmount;

}

