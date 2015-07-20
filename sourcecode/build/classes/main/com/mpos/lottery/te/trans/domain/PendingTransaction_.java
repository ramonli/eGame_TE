package com.mpos.lottery.te.trans.domain;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PendingTransaction.class)
public abstract class PendingTransaction_ {

	public static volatile SingularAttribute<PendingTransaction, String> id;
	public static volatile SingularAttribute<PendingTransaction, Date> createTime;
	public static volatile SingularAttribute<PendingTransaction, Integer> transType;
	public static volatile SingularAttribute<PendingTransaction, Boolean> dealed;
	public static volatile SingularAttribute<PendingTransaction, Integer> createdBy;
	public static volatile SingularAttribute<PendingTransaction, Date> updateTime;
	public static volatile SingularAttribute<PendingTransaction, String> ticketSerialNo;
	public static volatile SingularAttribute<PendingTransaction, String> traceMsgId;
	public static volatile SingularAttribute<PendingTransaction, Long> deviceId;
	public static volatile SingularAttribute<PendingTransaction, Integer> updatedBy;

}

