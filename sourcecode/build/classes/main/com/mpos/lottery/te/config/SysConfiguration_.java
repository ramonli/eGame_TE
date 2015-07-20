package com.mpos.lottery.te.config;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SysConfiguration.class)
public abstract class SysConfiguration_ {

	public static volatile SingularAttribute<SysConfiguration, Integer> manualSettlementHandlingMode;
	public static volatile SingularAttribute<SysConfiguration, Integer> maxiumTimesOfCashoutPass;
	public static volatile SingularAttribute<SysConfiguration, Boolean> isNeedInputPin;
	public static volatile SingularAttribute<SysConfiguration, Integer> maxExpireTimeCashoutPass;
	public static volatile SingularAttribute<SysConfiguration, Boolean> needInputAmount;
	public static volatile SingularAttribute<SysConfiguration, Integer> incentiveTarget;
	public static volatile SingularAttribute<SysConfiguration, String> licenseFilePath;
	public static volatile SingularAttribute<SysConfiguration, Boolean> encryptSerialNo;
	public static volatile SingularAttribute<SysConfiguration, Boolean> genValidationCode;
	public static volatile SingularAttribute<SysConfiguration, Integer> serverType;
	public static volatile SingularAttribute<SysConfiguration, String> id;
	public static volatile SingularAttribute<SysConfiguration, Boolean> incentiveEnabled;
	public static volatile SingularAttribute<SysConfiguration, Boolean> restoreCreditLevelWhenPayout;
	public static volatile SingularAttribute<SysConfiguration, String> serialNoPublicKeyPath;
	public static volatile SingularAttribute<SysConfiguration, Boolean> supportCommissionCalculation;
	public static volatile SingularAttribute<SysConfiguration, String> appKey;
	public static volatile SingularAttribute<SysConfiguration, Long> maxAllowedDaysOfActivityReport;

}

