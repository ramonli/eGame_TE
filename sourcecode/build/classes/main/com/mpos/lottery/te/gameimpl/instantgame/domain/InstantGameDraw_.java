package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.gamespec.game.Game;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(InstantGameDraw.class)
public abstract class InstantGameDraw_ {

	public static volatile SingularAttribute<InstantGameDraw, String> id;
	public static volatile SingularAttribute<InstantGameDraw, Date> startActivationTime;
	public static volatile SingularAttribute<InstantGameDraw, Integer> isSuspendActiveBlocked;
	public static volatile SingularAttribute<InstantGameDraw, Integer> status;
	public static volatile SingularAttribute<InstantGameDraw, Date> stopActivationTime;
	public static volatile SingularAttribute<InstantGameDraw, String> prizeLogicId;
	public static volatile SingularAttribute<InstantGameDraw, Integer> validationType;
	public static volatile SingularAttribute<InstantGameDraw, String> name;
	public static volatile SingularAttribute<InstantGameDraw, Game> game;
	public static volatile SingularAttribute<InstantGameDraw, Integer> isSuspendPayoutBlocked;
	public static volatile SingularAttribute<InstantGameDraw, BigDecimal> ticketFaceValue;
	public static volatile SingularAttribute<InstantGameDraw, Date> stopPayoutTime;

}

