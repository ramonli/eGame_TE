package com.mpos.lottery.te.valueaddservice.vat;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(VatOperatorBalance.class)
public abstract class VatOperatorBalance_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<VatOperatorBalance, BigDecimal> payoutBalance;
	public static volatile SingularAttribute<VatOperatorBalance, String> operatorId;
	public static volatile SingularAttribute<VatOperatorBalance, BigDecimal> saleBalance;

}

