package com.exploration.container.packing.web.models;

import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.Item;
import lombok.Data;

import java.util.List;

@Data
public class ContainerPackingRequest {
    private List<Container> containers;

    private List<Item> ItemsToPack;

    private List<Integer> AlgorithmTypeIDs;
}