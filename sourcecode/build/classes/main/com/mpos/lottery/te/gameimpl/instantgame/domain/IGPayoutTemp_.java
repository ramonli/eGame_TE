package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.trans.domain.Transaction;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(IGPayoutTemp.class)
public abstract class IGPayoutTemp_ extends com.mpos.lottery.te.common.dao.VersionEntity_ {

	public static volatile SingularAttribute<IGPayoutTemp, Integer> inputChannel;
	public static volatile SingularAttribute<IGPayoutTemp, BigDecimal> beforeTaxTotalAmount;
	public static volatile SingularAttribute<IGPayoutTemp, String> operatorId;
	public static volatile SingularAttribute<IGPayoutTemp, Transaction> transaction;
	public static volatile SingularAttribute<IGPayoutTemp, Integer> status;
	public static volatile SingularAttribute<IGPayoutTemp, String> ticketSerialNo;
	public static volatile SingularAttribute<IGPayoutTemp, String> gameId;
	public static volatile SingularAttribute<IGPayoutTemp, String> gameInstanceId;
	public static volatile SingularAttribute<IGPayoutTemp, Integer> type;
	public static volatile SingularAttribute<IGPayoutTemp, Long> iGBatchNumber;
	public static volatile SingularAttribute<IGPayoutTemp, BigDecimal> totalAmount;
	public static volatile SingularAttribute<IGPayoutTemp, Integer> merchantId;
	public static volatile SingularAttribute<IGPayoutTemp, BigDecimal> beforeTaxObjectAmount;
	public static volatile SingularAttribute<IGPayoutTemp, Long> devId;
	public static volatile SingularAttribute<IGPayoutTemp, Integer> numberOfObject;
	public static volatile SingularAttribute<IGPayoutTemp, Boolean> isValid;

}

