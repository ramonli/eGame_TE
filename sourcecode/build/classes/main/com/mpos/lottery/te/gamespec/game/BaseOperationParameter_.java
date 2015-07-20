package com.mpos.lottery.te.gamespec.game;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseOperationParameter.class)
public abstract class BaseOperationParameter_ {

	public static volatile SingularAttribute<BaseOperationParameter, String> id;
	public static volatile SingularAttribute<BaseOperationParameter, BigDecimal> baseAmount;
	public static volatile SingularAttribute<BaseOperationParameter, Boolean> allowSaleOnNewGameInstance;
	public static volatile SingularAttribute<BaseOperationParameter, Boolean> isBankerBetOptionSupported;
	public static volatile SingularAttribute<BaseOperationParameter, Boolean> isMultipleBetOptionSupported;
	public static volatile SingularAttribute<BaseOperationParameter, Boolean> allowManualCancellation;
	public static volatile SingularAttribute<BaseOperationParameter, Integer> minAllowedMultiDraw;
	public static volatile SingularAttribute<BaseOperationParameter, Integer> maxAllowedMultiDraw;
	public static volatile SingularAttribute<BaseOperationParameter, Integer> payoutMode;

}

