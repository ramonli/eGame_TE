package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(IGBatchReport.class)
public abstract class IGBatchReport_ {

	public static volatile SingularAttribute<IGBatchReport, String> updateBy;
	public static volatile SingularAttribute<IGBatchReport, String> id;
	public static volatile SingularAttribute<IGBatchReport, Date> createTime;
	public static volatile SingularAttribute<IGBatchReport, BigDecimal> taxAmount;
	public static volatile SingularAttribute<IGBatchReport, Long> succeededTicketsCount;
	public static volatile SingularAttribute<IGBatchReport, String> operatorId;
	public static volatile SingularAttribute<IGBatchReport, Date> updateTime;
	public static volatile SingularAttribute<IGBatchReport, Long> batchId;
	public static volatile SingularAttribute<IGBatchReport, String> createBy;
	public static volatile SingularAttribute<IGBatchReport, BigDecimal> actualAmount;
	public static volatile SingularAttribute<IGBatchReport, Long> failedTicketsCount;

}

