package com.mpos.lottery.te.gameimpl.lotto.draw;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LottoOperationParameter.class)
public abstract class LottoOperationParameter_ extends com.mpos.lottery.te.gamespec.game.BaseOperationParameter_ {

	public static volatile SingularAttribute<LottoOperationParameter, Boolean> isAllowOfflineSale;
	public static volatile SingularAttribute<LottoOperationParameter, Integer> offlineUploadDeadline;
	public static volatile SingularAttribute<LottoOperationParameter, Integer> maxMultipleCount;

}

