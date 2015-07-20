package com.mpos.lottery.te.valueaddservice.voucher;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(VoucherStatistics.class)
public abstract class VoucherStatistics_ {

	public static volatile SingularAttribute<VoucherStatistics, String> id;
	public static volatile SingularAttribute<VoucherStatistics, String> gameId;
	public static volatile SingularAttribute<VoucherStatistics, BigDecimal> faceAmount;
	public static volatile SingularAttribute<VoucherStatistics, Integer> remainCount;

}

