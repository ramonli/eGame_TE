package com.mpos.lottery.te.gamespec.game;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ChannelGameInstanceTime.class)
public abstract class ChannelGameInstanceTime_ {

	public static volatile SingularAttribute<ChannelGameInstanceTime, String> id;
	public static volatile SingularAttribute<ChannelGameInstanceTime, String> gameChannelSettingId;
	public static volatile SingularAttribute<ChannelGameInstanceTime, Integer> stopSellingTimeInMinutes;
	public static volatile SingularAttribute<ChannelGameInstanceTime, Integer> channelType;

}

