package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(IGFailedTicketsReport.class)
public abstract class IGFailedTicketsReport_ {

	public static volatile SingularAttribute<IGFailedTicketsReport, String> updateBy;
	public static volatile SingularAttribute<IGFailedTicketsReport, String> id;
	public static volatile SingularAttribute<IGFailedTicketsReport, Date> createTime;
	public static volatile SingularAttribute<IGFailedTicketsReport, String> operatorId;
	public static volatile SingularAttribute<IGFailedTicketsReport, Date> updateTime;
	public static volatile SingularAttribute<IGFailedTicketsReport, Integer> status;
	public static volatile SingularAttribute<IGFailedTicketsReport, Integer> errorCode;
	public static volatile SingularAttribute<IGFailedTicketsReport, String> igSerialNumber;
	public static volatile SingularAttribute<IGFailedTicketsReport, Long> batchId;
	public static volatile SingularAttribute<IGFailedTicketsReport, String> createBy;

}

