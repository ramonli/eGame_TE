package com.mpos.lottery.te.gamespec.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(InstantaneousSale.class)
public abstract class InstantaneousSale_ {

	public static volatile SingularAttribute<InstantaneousSale, String> id;
	public static volatile SingularAttribute<InstantaneousSale, Integer> cancelTicketCount;
	public static volatile SingularAttribute<InstantaneousSale, BigDecimal> turnover;
	public static volatile SingularAttribute<InstantaneousSale, Integer> saleCount;
	public static volatile SingularAttribute<InstantaneousSale, String> gameInstanceId;

}

