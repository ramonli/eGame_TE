package com.mpos.lottery.te.gamespec.prize;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TaxDateRange.class)
public abstract class TaxDateRange_ {

	public static volatile SingularAttribute<TaxDateRange, String> id;
	public static volatile SingularAttribute<TaxDateRange, Date> endDate;
	public static volatile SingularAttribute<TaxDateRange, Date> beginDate;

}

