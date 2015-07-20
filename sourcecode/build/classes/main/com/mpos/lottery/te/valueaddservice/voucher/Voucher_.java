package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.te.gamespec.game.Game;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Voucher.class)
public abstract class Voucher_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<Voucher, String> serialNo;
	public static volatile SingularAttribute<Voucher, String> batchNo;
	public static volatile SingularAttribute<Voucher, Integer> status;
	public static volatile SingularAttribute<Voucher, String> pin;
	public static volatile SingularAttribute<Voucher, Date> expireDate;
	public static volatile SingularAttribute<Voucher, BigDecimal> faceAmount;
	public static volatile SingularAttribute<Voucher, Game> game;
	public static volatile SingularAttribute<Voucher, String> currencyType;

}

