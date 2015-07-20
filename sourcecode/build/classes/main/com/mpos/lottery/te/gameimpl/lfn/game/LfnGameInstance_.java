package com.mpos.lottery.te.gameimpl.lfn.game;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LfnGameInstance.class)
public abstract class LfnGameInstance_ extends com.mpos.lottery.te.gamespec.game.BaseGameInstance_ {

	public static volatile SingularAttribute<LfnGameInstance, Date> payoutStartTime;
	public static volatile SingularAttribute<LfnGameInstance, String> prizeLogicId;

}

