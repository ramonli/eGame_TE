package com.mpos.lottery.te.gamespec.game;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseFunType implements java.io.Serializable {
    private static final long serialVersionUID = -2639363860618705002L;
    @Id
    @Column(name = "ID")
    // // create seqence TE_SEQ start with 1 increment by 1;
    // @SequenceGenerator(name="TE_SEQ", sequenceName="TE_SEQ")
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="TE_SEQ")
    private String id;

    // the k part of K/N game, such as 6/49, k=6
    @Column(name = "KKK")
    private int k;

    // the n part of K/N game, such as 6/49, n=49
    @Column(name = "NNN")
    private int n;

    @Column(name = "XXX")
    private int x;

    @Column(name = "YYY")
    private int y;

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
