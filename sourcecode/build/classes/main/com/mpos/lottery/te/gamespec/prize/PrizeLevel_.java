package com.mpos.lottery.te.gamespec.prize;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PrizeLevel.class)
public abstract class PrizeLevel_ {

	public static volatile SingularAttribute<PrizeLevel, String> id;
	public static volatile SingularAttribute<PrizeLevel, Date> updateTime;
	public static volatile SingularAttribute<PrizeLevel, Integer> numberOfWinner;
	public static volatile SingularAttribute<PrizeLevel, String> prizeLogicId;
	public static volatile ListAttribute<PrizeLevel, PrizeLevelItem> levelItems;
	public static volatile SingularAttribute<PrizeLevel, Integer> prizeType;
	public static volatile SingularAttribute<PrizeLevel, Integer> prizeLevel;

}

