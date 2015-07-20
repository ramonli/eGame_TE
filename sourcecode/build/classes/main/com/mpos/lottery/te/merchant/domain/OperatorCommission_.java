package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.gamespec.game.Game;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OperatorCommission.class)
public abstract class OperatorCommission_ {

	public static volatile SingularAttribute<OperatorCommission, String> id;
	public static volatile SingularAttribute<OperatorCommission, BigDecimal> saleRate;
	public static volatile SingularAttribute<OperatorCommission, String> operatorId;
	public static volatile SingularAttribute<OperatorCommission, BigDecimal> payoutRate;
	public static volatile SingularAttribute<OperatorCommission, Long> merchantId;
	public static volatile SingularAttribute<OperatorCommission, Game> game;

}

