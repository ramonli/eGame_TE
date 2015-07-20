package com.mpos.lottery.te.gameimpl.bingo.sale;

import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BingoTicket.class)
public abstract class BingoTicket_ extends com.mpos.lottery.te.gamespec.sale.BaseTicket_ {

	public static volatile SingularAttribute<BingoTicket, String> importedSerialNo;
	public static volatile SingularAttribute<BingoTicket, String> bookNo;
	public static volatile SingularAttribute<BingoTicket, String> luckySerial;
	public static volatile SingularAttribute<BingoTicket, BingoGameInstance> gameInstance;

}

