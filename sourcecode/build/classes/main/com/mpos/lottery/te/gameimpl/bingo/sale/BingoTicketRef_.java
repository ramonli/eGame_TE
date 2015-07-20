package com.mpos.lottery.te.gameimpl.bingo.sale;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BingoTicketRef.class)
public abstract class BingoTicketRef_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<BingoTicketRef, String> pin;
	public static volatile SingularAttribute<BingoTicketRef, Integer> status;
	public static volatile SingularAttribute<BingoTicketRef, Long> sequence;
	public static volatile SingularAttribute<BingoTicketRef, String> bookNo;
	public static volatile SingularAttribute<BingoTicketRef, String> importTicketSerialNo;
	public static volatile SingularAttribute<BingoTicketRef, String> luckySerial;
	public static volatile SingularAttribute<BingoTicketRef, BigDecimal> totalAmount;
	public static volatile SingularAttribute<BingoTicketRef, String> gameInstanceId;

}

