package com.mpos.lottery.te.gamespec.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseEntry.class)
public abstract class BaseEntry_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<BaseEntry, Integer> inputChannel;
	public static volatile SingularAttribute<BaseEntry, Integer> betOption;
	public static volatile SingularAttribute<BaseEntry, BigDecimal> entryAmount;
	public static volatile SingularAttribute<BaseEntry, Long> totalBets;
	public static volatile SingularAttribute<BaseEntry, String> ticketSerialNo;
	public static volatile SingularAttribute<BaseEntry, String> selectNumber;
	public static volatile SingularAttribute<BaseEntry, String> entryNo;

}

