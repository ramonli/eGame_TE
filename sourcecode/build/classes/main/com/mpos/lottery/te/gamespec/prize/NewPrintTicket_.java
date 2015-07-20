package com.mpos.lottery.te.gamespec.prize;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(NewPrintTicket.class)
public abstract class NewPrintTicket_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<NewPrintTicket, Integer> status;
	public static volatile SingularAttribute<NewPrintTicket, String> oldTicketSerialNo;
	public static volatile SingularAttribute<NewPrintTicket, String> newTicketSerialNo;

}

