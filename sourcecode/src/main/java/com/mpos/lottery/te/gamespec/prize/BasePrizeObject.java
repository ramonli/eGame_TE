package com.mpos.lottery.te.gamespec.prize;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BD_PRIZE_OBJECT")
public class BasePrizeObject {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "PRICE")
    private BigDecimal price = new BigDecimal("0");
    @Column(name = "TAX_AMOUNT")
    private BigDecimal tax = new BigDecimal("0");
    @Column(name = "OBJECT_NAME")
    private String name;
    // refer to ObjectPrizeLevelDefItem.type_XXX
    @Column(name = "OBJECT_TYPE")
    private int type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
