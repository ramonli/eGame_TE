package com.mpos.lottery.te.gamespec.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OfflineTicketLog.class)
public abstract class OfflineTicketLog_ extends com.mpos.lottery.te.common.dao.SettlementEntity_ {

	public static volatile SingularAttribute<OfflineTicketLog, Integer> statusCode;
	public static volatile SingularAttribute<OfflineTicketLog, String> serialNo;
	public static volatile SingularAttribute<OfflineTicketLog, String> transactionId;
	public static volatile SingularAttribute<OfflineTicketLog, Integer> gameType;
	public static volatile SingularAttribute<OfflineTicketLog, Integer> status;
	public static volatile SingularAttribute<OfflineTicketLog, Integer> ticketType;
	public static volatile SingularAttribute<OfflineTicketLog, String> gameId;
	public static volatile SingularAttribute<OfflineTicketLog, BigDecimal> totalAmount;
	public static volatile SingularAttribute<OfflineTicketLog, String> gameInstanceId;
	public static volatile SingularAttribute<OfflineTicketLog, Integer> ticketFrom;
	public static volatile SingularAttribute<OfflineTicketLog, String> uploadedGameInstanceId;

}

