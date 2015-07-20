package com.mpos.lottery.te.merchant.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Device.class)
public abstract class Device_ {

	public static volatile SingularAttribute<Device, Long> id;
	public static volatile SingularAttribute<Device, String> serialNo;
	public static volatile SingularAttribute<Device, Integer> status;
	public static volatile SingularAttribute<Device, Merchant> merchant;
	public static volatile SingularAttribute<Device, String> hardwareId;
	public static volatile SingularAttribute<Device, String> departmentId;
	public static volatile SingularAttribute<Device, String> type;

}

