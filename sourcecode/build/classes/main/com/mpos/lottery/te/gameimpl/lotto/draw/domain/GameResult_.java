package com.mpos.lottery.te.gameimpl.lotto.draw.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(GameResult.class)
public abstract class GameResult_ {

	public static volatile SingularAttribute<GameResult, String> id;
	public static volatile SingularAttribute<GameResult, String> baseNumber;
	public static volatile SingularAttribute<GameResult, LottoGameInstance> gameDraw;
	public static volatile SingularAttribute<GameResult, Integer> specialNumber;

}

