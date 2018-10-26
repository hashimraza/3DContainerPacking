package com.exploration.container.packing.entities;

import java.util.ArrayList;
import java.util.List;

public class ContainerPackingResult {
    public int containerId;
    public List<AlgorithmPackingResult> algorithmPackingResults;

    public ContainerPackingResult() {
        this.algorithmPackingResults = new ArrayList<>();
    }
}