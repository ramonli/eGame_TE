package com.mpos.lottery.te.valueaddservice.vat;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(VatMerchantBalance.class)
public abstract class VatMerchantBalance_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<VatMerchantBalance, BigDecimal> payoutBalance;
	public static volatile SingularAttribute<VatMerchantBalance, Long> merchantId;
	public static volatile SingularAttribute<VatMerchantBalance, BigDecimal> saleBalance;

}

