package com.mpos.lottery.te.gameimpl.magic100.sale;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LuckyNumberSequence.class)
public abstract class LuckyNumberSequence_ {

	public static volatile SingularAttribute<LuckyNumberSequence, String> id;
	public static volatile SingularAttribute<LuckyNumberSequence, Long> nextSequence;
	public static volatile SingularAttribute<LuckyNumberSequence, String> gameId;
	public static volatile SingularAttribute<LuckyNumberSequence, String> lastestPlayer;

}

