package com.zooplus.logistics.exploration.packing.web.Models;

import com.zooplus.logistics.exploration.packing.entities.Container;
import com.zooplus.logistics.exploration.packing.entities.Item;
import lombok.Data;

import java.util.List;

@Data
public class ContainerPackingRequest {
    public List<Container> containers;

    public List<Item> ItemsToPack;

    public List<Integer> AlgorithmTypeIDs;
}