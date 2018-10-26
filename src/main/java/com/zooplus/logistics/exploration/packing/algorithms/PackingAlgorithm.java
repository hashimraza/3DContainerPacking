package com.zooplus.logistics.exploration.packing.algorithms;

import com.zooplus.logistics.exploration.packing.entities.AlgorithmPackingResult;
import com.zooplus.logistics.exploration.packing.entities.Container;
import com.zooplus.logistics.exploration.packing.entities.Item;

import java.util.List;

public interface PackingAlgorithm {
    AlgorithmPackingResult run(Container container, List<Item> items);
}