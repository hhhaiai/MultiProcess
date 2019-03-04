package com.analysys.track.model;

public enum  EGEnum {
    ZERO(0),ONE(1);

    private final int value;

    // 构造器默认也只能是private, 从而保证构造函数只能在内部使用
    EGEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
