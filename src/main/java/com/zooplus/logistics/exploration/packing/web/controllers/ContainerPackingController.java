package com.zooplus.logistics.exploration.packing.web.controllers;

import com.zooplus.logistics.exploration.packing.entities.ContainerPackingResult;
import com.zooplus.logistics.exploration.packing.service.PackingService;
import com.zooplus.logistics.exploration.packing.web.Models.ContainerPackingRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/containerpacking")
public class ContainerPackingController {
    /// <summary>
    /// Posts the specified packing request.
    /// </summary>
    /// <param name="request">The packing request.</param>
    /// <returns>A container packing result with lists of packed and unpacked items.</returns>
    @PostMapping
    public List<ContainerPackingResult> Post(@RequestBody ContainerPackingRequest request) {
        return new PackingService().pack(request.containers, request.ItemsToPack, request.AlgorithmTypeIDs);
    }
}