package com.mpos.lottery.te.valueaddservice.airtime;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.trans.domain.Transaction;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AirtimeTopup.class)
public abstract class AirtimeTopup_ extends com.mpos.lottery.te.common.dao.SettlementEntity_ {

	public static volatile SingularAttribute<AirtimeTopup, Integer> gpeSourceType;
	public static volatile SingularAttribute<AirtimeTopup, BigDecimal> amount;
	public static volatile SingularAttribute<AirtimeTopup, String> serialNo;
	public static volatile SingularAttribute<AirtimeTopup, Transaction> transaction;
	public static volatile SingularAttribute<AirtimeTopup, Integer> status;
	public static volatile SingularAttribute<AirtimeTopup, Game> game;
	public static volatile SingularAttribute<AirtimeTopup, String> mobileNo;
	public static volatile SingularAttribute<AirtimeTopup, String> telcCommTransId;

}

