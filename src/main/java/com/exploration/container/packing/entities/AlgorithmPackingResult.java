package com.exploration.container.packing.entities;

import java.util.ArrayList;

public class AlgorithmPackingResult {
    public int algorithmId;

    public String algorithmName;
    public boolean isCompletePack;

    public ArrayList<Item> packedItems;
    public long packTimeInMilliseconds;

    public double percentContainerVolumePacked;
    public double percentItemVolumePacked;

    public ArrayList<Item> unpackedItems;

    public AlgorithmPackingResult() {
        this.packedItems = new ArrayList<>();
        this.unpackedItems = new ArrayList<>();
    }
}
