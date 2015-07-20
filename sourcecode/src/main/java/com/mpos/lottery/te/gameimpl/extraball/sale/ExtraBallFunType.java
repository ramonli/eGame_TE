package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.gamespec.game.BaseFunType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "EB_FUN_TYPE")
public class ExtraBallFunType extends BaseFunType {
    private static final long serialVersionUID = -5751414608249153821L;
    @Column(name = "FUN_NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
