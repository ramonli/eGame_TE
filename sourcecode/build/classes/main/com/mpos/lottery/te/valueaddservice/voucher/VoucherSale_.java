package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.trans.domain.Transaction;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(VoucherSale.class)
public abstract class VoucherSale_ extends com.mpos.lottery.te.common.dao.SettlementEntity_ {

	public static volatile SingularAttribute<VoucherSale, Integer> status;
	public static volatile SingularAttribute<VoucherSale, Transaction> transaction;
	public static volatile SingularAttribute<VoucherSale, String> voucherSerialNo;
	public static volatile SingularAttribute<VoucherSale, Game> game;
	public static volatile SingularAttribute<VoucherSale, BigDecimal> voucherFaceAmount;
	public static volatile SingularAttribute<VoucherSale, String> voucherId;

}

