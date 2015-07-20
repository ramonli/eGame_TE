package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LuckyNumber.class)
public abstract class LuckyNumber_ {

	public static volatile SingularAttribute<LuckyNumber, String> id;
	public static volatile SingularAttribute<LuckyNumber, Integer> cancelCounter;
	public static volatile SingularAttribute<LuckyNumber, Long> sequenceOfNumber;
	public static volatile SingularAttribute<LuckyNumber, Date> updateTime;
	public static volatile SingularAttribute<LuckyNumber, BigDecimal> prizeAmount;
	public static volatile SingularAttribute<LuckyNumber, Magic100GameInstance> gameInstance;
	public static volatile SingularAttribute<LuckyNumber, String> luckyNumber;

}

