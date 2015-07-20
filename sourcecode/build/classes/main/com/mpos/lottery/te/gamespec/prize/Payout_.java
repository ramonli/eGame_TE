package com.mpos.lottery.te.gamespec.prize;

import com.mpos.lottery.te.trans.domain.Transaction;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Payout.class)
public abstract class Payout_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<Payout, Integer> inputChannel;
	public static volatile SingularAttribute<Payout, BigDecimal> beforeTaxTotalAmount;
	public static volatile SingularAttribute<Payout, String> operatorId;
	public static volatile SingularAttribute<Payout, Transaction> transaction;
	public static volatile SingularAttribute<Payout, Integer> status;
	public static volatile SingularAttribute<Payout, String> ticketSerialNo;
	public static volatile SingularAttribute<Payout, String> gameId;
	public static volatile SingularAttribute<Payout, String> gameInstanceId;
	public static volatile SingularAttribute<Payout, Integer> type;
	public static volatile SingularAttribute<Payout, BigDecimal> totalAmount;
	public static volatile SingularAttribute<Payout, Integer> merchantId;
	public static volatile SingularAttribute<Payout, BigDecimal> beforeTaxObjectAmount;
	public static volatile SingularAttribute<Payout, Long> devId;
	public static volatile SingularAttribute<Payout, Integer> numberOfObject;
	public static volatile SingularAttribute<Payout, Boolean> isValid;

}

