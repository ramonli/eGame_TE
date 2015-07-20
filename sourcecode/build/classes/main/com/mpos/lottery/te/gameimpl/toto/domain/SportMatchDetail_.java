package com.mpos.lottery.te.gameimpl.toto.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SportMatchDetail.class)
public abstract class SportMatchDetail_ {

	public static volatile SingularAttribute<SportMatchDetail, String> matchTypeId;
	public static volatile SingularAttribute<SportMatchDetail, String> result;
	public static volatile SingularAttribute<SportMatchDetail, Integer> quarterType;
	public static volatile SingularAttribute<SportMatchDetail, BigDecimal> winOption;
	public static volatile SingularAttribute<SportMatchDetail, Integer> matchSeq;
	public static volatile SingularAttribute<SportMatchDetail, String> awayId;
	public static volatile SingularAttribute<SportMatchDetail, String> gameInstanceId;
	public static volatile SingularAttribute<SportMatchDetail, String> homeId;
	public static volatile SingularAttribute<SportMatchDetail, String> matchDetailId;
	public static volatile SingularAttribute<SportMatchDetail, String> betTypeId;
	public static volatile SingularAttribute<SportMatchDetail, BigDecimal> betOptionValue;
	public static volatile SingularAttribute<SportMatchDetail, Integer> quarterValue;
	public static volatile SingularAttribute<SportMatchDetail, Date> gameDateTime;
	public static volatile SingularAttribute<SportMatchDetail, BigDecimal> countInPool;
	public static volatile SingularAttribute<SportMatchDetail, String> betoptionId;

}

