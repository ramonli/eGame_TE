package com.mpos.lottery.te.gameimpl.magic100.sale;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OfflineCancellation.class)
public abstract class OfflineCancellation_ {

	public static volatile SingularAttribute<OfflineCancellation, String> updateBy;
	public static volatile SingularAttribute<OfflineCancellation, String> id;
	public static volatile SingularAttribute<OfflineCancellation, Date> createTime;
	public static volatile SingularAttribute<OfflineCancellation, Long> startNumber;
	public static volatile SingularAttribute<OfflineCancellation, Date> updateTime;
	public static volatile SingularAttribute<OfflineCancellation, String> gameId;
	public static volatile SingularAttribute<OfflineCancellation, Integer> isHandled;
	public static volatile SingularAttribute<OfflineCancellation, String> teTransactionId;
	public static volatile SingularAttribute<OfflineCancellation, Long> endNumber;
	public static volatile SingularAttribute<OfflineCancellation, String> createBy;
	public static volatile SingularAttribute<OfflineCancellation, Long> currentNumber;

}

