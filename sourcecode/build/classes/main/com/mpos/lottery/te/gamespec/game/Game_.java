package com.mpos.lottery.te.gamespec.game;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Game.class)
public abstract class Game_ {

	public static volatile SingularAttribute<Game, Boolean> needAutoPayout;
	public static volatile SingularAttribute<Game, String> operatorParameterId;
	public static volatile SingularAttribute<Game, String> id;
	public static volatile SingularAttribute<Game, String> taxPolicyId;
	public static volatile SingularAttribute<Game, String> ticketLogo;
	public static volatile SingularAttribute<Game, String> name;
	public static volatile SingularAttribute<Game, Integer> state;
	public static volatile SingularAttribute<Game, Integer> taxMethodBase;
	public static volatile SingularAttribute<Game, Integer> taxMethod;
	public static volatile SingularAttribute<Game, String> funTypeId;
	public static volatile SingularAttribute<Game, Integer> type;
	public static volatile SingularAttribute<Game, String> legalNum;

}

