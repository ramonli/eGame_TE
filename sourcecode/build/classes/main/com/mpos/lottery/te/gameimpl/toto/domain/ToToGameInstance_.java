package com.mpos.lottery.te.gameimpl.toto.domain;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ToToGameInstance.class)
public abstract class ToToGameInstance_ extends com.mpos.lottery.te.gamespec.game.BaseGameInstance_ {

	public static volatile SingularAttribute<ToToGameInstance, BigDecimal> baseAmount;
	public static volatile SingularAttribute<ToToGameInstance, String> prizeLogicId;
	public static volatile SingularAttribute<ToToGameInstance, String> omrGameSet;
	public static volatile SingularAttribute<ToToGameInstance, BigDecimal> matchNum;

}

