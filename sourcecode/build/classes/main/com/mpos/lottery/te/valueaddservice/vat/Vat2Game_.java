package com.mpos.lottery.te.valueaddservice.vat;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Vat2Game.class)
public abstract class Vat2Game_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<Vat2Game, BigDecimal> rate;
	public static volatile SingularAttribute<Vat2Game, Integer> status;
	public static volatile SingularAttribute<Vat2Game, String> gameId;
	public static volatile SingularAttribute<Vat2Game, String> businessType;
	public static volatile SingularAttribute<Vat2Game, BigDecimal> minThresholdAmount;
	public static volatile SingularAttribute<Vat2Game, String> vatId;

}

