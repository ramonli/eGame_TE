package com.mpos.lottery.te.merchant.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SmartCard.class)
public abstract class SmartCard_ {

	public static volatile SingularAttribute<SmartCard, String> id;
	public static volatile SingularAttribute<SmartCard, String> serialNo;
	public static volatile SingularAttribute<SmartCard, Integer> status;
	public static volatile SingularAttribute<SmartCard, String> PIN;
	public static volatile SingularAttribute<SmartCard, Long> merchantId;

}

