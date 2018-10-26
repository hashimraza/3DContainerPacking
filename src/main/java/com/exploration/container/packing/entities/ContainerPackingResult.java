package com.exploration.container.packing.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContainerPackingResult {
    private int containerId;
    private List<AlgorithmPackingResult> algorithmPackingResults;

    public ContainerPackingResult() {
        this.setAlgorithmPackingResults(new ArrayList<>());
    }
}