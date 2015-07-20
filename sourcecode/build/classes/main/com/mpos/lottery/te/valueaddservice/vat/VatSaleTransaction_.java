package com.mpos.lottery.te.valueaddservice.vat;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(VatSaleTransaction.class)
public abstract class VatSaleTransaction_ extends com.mpos.lottery.te.common.dao.BaseEntity_ {

	public static volatile SingularAttribute<VatSaleTransaction, String> sellerCompanyId;
	public static volatile SingularAttribute<VatSaleTransaction, String> buyerCompanyId;
	public static volatile SingularAttribute<VatSaleTransaction, String> transactionId;
	public static volatile SingularAttribute<VatSaleTransaction, String> operatorId;
	public static volatile SingularAttribute<VatSaleTransaction, Integer> gameType;
	public static volatile SingularAttribute<VatSaleTransaction, Integer> status;
	public static volatile SingularAttribute<VatSaleTransaction, String> vatRefNo;
	public static volatile SingularAttribute<VatSaleTransaction, String> ticketSerialNo;
	public static volatile SingularAttribute<VatSaleTransaction, Date> transTimestampRef;
	public static volatile SingularAttribute<VatSaleTransaction, String> businessType;
	public static volatile SingularAttribute<VatSaleTransaction, String> gameInstanceId;
	public static volatile SingularAttribute<VatSaleTransaction, BigDecimal> saleTotalAmount;
	public static volatile SingularAttribute<VatSaleTransaction, String> vatId;
	public static volatile SingularAttribute<VatSaleTransaction, BigDecimal> vatTotalAmount;
	public static volatile SingularAttribute<VatSaleTransaction, Long> merchantId;
	public static volatile SingularAttribute<VatSaleTransaction, BigDecimal> vatRateTotalAmount;

}

