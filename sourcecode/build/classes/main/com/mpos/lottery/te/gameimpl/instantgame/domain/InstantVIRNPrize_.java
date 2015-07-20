package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(InstantVIRNPrize.class)
public abstract class InstantVIRNPrize_ {

	public static volatile SingularAttribute<InstantVIRNPrize, BigDecimal> actualPayout;
	public static volatile SingularAttribute<InstantVIRNPrize, String> id;
	public static volatile SingularAttribute<InstantVIRNPrize, BigDecimal> taxAmount;
	public static volatile SingularAttribute<InstantVIRNPrize, String> virn;
	public static volatile SingularAttribute<InstantVIRNPrize, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<InstantVIRNPrize, Boolean> isValidated;
	public static volatile SingularAttribute<InstantVIRNPrize, InstantGameDraw> gameDraw;
	public static volatile SingularAttribute<InstantVIRNPrize, String> prizeType;

}

