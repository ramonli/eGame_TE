package com.mpos.lottery.te.gameimpl.magic100.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Magic100Entry.class)
public abstract class Magic100Entry_ extends com.mpos.lottery.te.gamespec.sale.BaseEntry_ {

	public static volatile SingularAttribute<Magic100Entry, Long> sequenceOfNumber;
	public static volatile SingularAttribute<Magic100Entry, Boolean> winning;
	public static volatile SingularAttribute<Magic100Entry, BigDecimal> taxAmount;
	public static volatile SingularAttribute<Magic100Entry, BigDecimal> prizeAmount;

}

