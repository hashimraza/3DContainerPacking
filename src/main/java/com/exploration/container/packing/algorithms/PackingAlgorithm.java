package com.exploration.container.packing.algorithms;

import com.exploration.container.packing.entities.AlgorithmPackingResult;
import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.Item;

import java.util.List;

public interface PackingAlgorithm {
    AlgorithmPackingResult run(Container container, List<Item> items);
}