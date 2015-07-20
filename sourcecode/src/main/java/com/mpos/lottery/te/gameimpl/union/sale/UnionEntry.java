package com.mpos.lottery.te.gameimpl.union.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UN_TE_ENTRY")
public class UnionEntry extends BaseEntry {

    private static final long serialVersionUID = -403385928050118329L;
}
