package com.mpos.lottery.te.trans.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TransactionMessage.class)
public abstract class TransactionMessage_ {

	public static volatile SingularAttribute<TransactionMessage, String> transactionId;
	public static volatile SingularAttribute<TransactionMessage, String> requestMsg;
	public static volatile SingularAttribute<TransactionMessage, String> responseMsg;

}

