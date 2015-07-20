package com.mpos.lottery.te.gamespec.game;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseGameInstance.class)
public abstract class BaseGameInstance_ {

	public static volatile SingularAttribute<BaseGameInstance, String> gameChannelSettingId;
	public static volatile SingularAttribute<BaseGameInstance, Integer> state;
	public static volatile SingularAttribute<BaseGameInstance, Date> beginTime;
	public static volatile SingularAttribute<BaseGameInstance, String> number;
	public static volatile SingularAttribute<BaseGameInstance, Game> game;
	public static volatile SingularAttribute<BaseGameInstance, BigDecimal> percentageOfTurnover;
	public static volatile SingularAttribute<BaseGameInstance, Date> endTime;
	public static volatile SingularAttribute<BaseGameInstance, Date> drawDate;
	public static volatile SingularAttribute<BaseGameInstance, Long> version;
	public static volatile SingularAttribute<BaseGameInstance, Integer> maxClaimDays;
	public static volatile SingularAttribute<BaseGameInstance, Boolean> suspendManualCancel;
	public static volatile SingularAttribute<BaseGameInstance, String> id;
	public static volatile SingularAttribute<BaseGameInstance, Integer> riskControlMethod;
	public static volatile SingularAttribute<BaseGameInstance, BigDecimal> maxLossAmount;
	public static volatile SingularAttribute<BaseGameInstance, String> name;
	public static volatile SingularAttribute<BaseGameInstance, Boolean> saleSuspended;
	public static volatile SingularAttribute<BaseGameInstance, Boolean> payoutBlocked;
	public static volatile SingularAttribute<BaseGameInstance, Date> freezeTime;

}

