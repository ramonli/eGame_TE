package com.mpos.lottery.te.common;

public class KeyValuePair<X, Y> {
    private X key;
    private Y value;

    /**
     * Constructor.
     */
    public KeyValuePair(X key, Y value) {
        if (key == null) {
            throw new IllegalArgumentException("argument 'key' can NOT be null.");
        }
        this.key = key;
        this.value = value;
    }

    public X getKey() {
        return key;
    }

    public Y getValue() {
        return value;
    }

}
