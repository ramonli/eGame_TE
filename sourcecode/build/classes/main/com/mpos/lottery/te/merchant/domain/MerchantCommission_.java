package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.gamespec.game.Game;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MerchantCommission.class)
public abstract class MerchantCommission_ {

	public static volatile SingularAttribute<MerchantCommission, String> id;
	public static volatile SingularAttribute<MerchantCommission, BigDecimal> saleCommissionRate;
	public static volatile SingularAttribute<MerchantCommission, Boolean> allowSale;
	public static volatile SingularAttribute<MerchantCommission, Long> merchantId;
	public static volatile SingularAttribute<MerchantCommission, Game> game;
	public static volatile SingularAttribute<MerchantCommission, Boolean> allowPayout;
	public static volatile SingularAttribute<MerchantCommission, BigDecimal> payoutCommissionRate;

}

