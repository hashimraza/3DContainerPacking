package com.exploration.container.packing.entities;

import lombok.Data;

import java.util.ArrayList;

@Data
public class AlgorithmPackingResult {
    private int algorithmId;

    private String algorithmName;
    private boolean isCompletePack;

    private ArrayList<Item> packedItems;
    private long packTimeInMilliseconds;

    private double percentContainerVolumePacked;
    private double percentItemVolumePacked;

    private ArrayList<Item> unpackedItems;

    public AlgorithmPackingResult() {
        this.setPackedItems(new ArrayList<>());
        this.setUnpackedItems(new ArrayList<>());
    }
}
