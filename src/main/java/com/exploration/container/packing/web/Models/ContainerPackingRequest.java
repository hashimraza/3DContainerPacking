package com.exploration.container.packing.web.Models;

import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.Item;
import lombok.Data;

import java.util.List;

@Data
public class ContainerPackingRequest {
    public List<Container> containers;

    public List<Item> ItemsToPack;

    public List<Integer> AlgorithmTypeIDs;
}