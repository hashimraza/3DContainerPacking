package com.zooplus.logistics.exploration.packing.algorithms;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AlgorithmType {
    EB_AFIT(1);

    private final int type;

    AlgorithmType(int type) {
        this.type = type;
    }

    public static AlgorithmType findByType(int type) {
        return Arrays.stream(values()).filter(at -> at.getType() == type).findFirst().orElse(null);

    }
}
