package com.mpos.lottery.te.gameimpl.bingo.game;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BingoGameInstance.class)
public abstract class BingoGameInstance_ extends com.mpos.lottery.te.gamespec.game.BaseGameInstance_ {

	public static volatile SingularAttribute<BingoGameInstance, String> luckyPrizeLogicId;
	public static volatile SingularAttribute<BingoGameInstance, Long> currentSequence;
	public static volatile SingularAttribute<BingoGameInstance, Long> endOfSequence;
	public static volatile SingularAttribute<BingoGameInstance, Long> startOfSequence;

}

