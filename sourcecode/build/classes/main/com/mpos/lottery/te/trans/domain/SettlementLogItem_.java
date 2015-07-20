package com.mpos.lottery.te.trans.domain;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SettlementLogItem.class)
public abstract class SettlementLogItem_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<SettlementLogItem, String> operatorId;
	public static volatile SingularAttribute<SettlementLogItem, SettlementLog> settlementLog;
	public static volatile SingularAttribute<SettlementLogItem, Date> checkDay;

}

