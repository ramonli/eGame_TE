package com.mpos.lottery.te.gameimpl.digital.game;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(DigitalOperationParameter.class)
public abstract class DigitalOperationParameter_ extends com.mpos.lottery.te.gamespec.game.BaseOperationParameter_ {

	public static volatile SingularAttribute<DigitalOperationParameter, Boolean> supportOddEven;
	public static volatile SingularAttribute<DigitalOperationParameter, Boolean> supportSum;
	public static volatile SingularAttribute<DigitalOperationParameter, BigDecimal> maxBetAmount;

}

