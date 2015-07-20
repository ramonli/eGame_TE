package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(InstantPrize.class)
public abstract class InstantPrize_ {

	public static volatile SingularAttribute<InstantPrize, BigDecimal> actualPayout;
	public static volatile SingularAttribute<InstantPrize, String> id;
	public static volatile SingularAttribute<InstantPrize, String> detail;
	public static volatile SingularAttribute<InstantPrize, BigDecimal> taxAmount;
	public static volatile SingularAttribute<InstantPrize, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<InstantPrize, InstantGameDraw> gameDraw;
	public static volatile SingularAttribute<InstantPrize, String> prizeType;
	public static volatile SingularAttribute<InstantPrize, Integer> prizeLevel;

}

