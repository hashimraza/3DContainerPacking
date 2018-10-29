package com.exploration.container.packing.web.controllers;

import com.exploration.container.packing.entities.ContainerPackingResult;
import com.exploration.container.packing.service.PackingService;
import com.exploration.container.packing.web.models.ContainerPackingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/containerpacking")
@RequiredArgsConstructor
public class ContainerPackingController {
    private final PackingService packingService;

    @PostMapping
    public List<ContainerPackingResult> Post(@RequestBody ContainerPackingRequest request) {
        return packingService.pack(request.getContainers(), request.getItemsToPack(), request.getAlgorithmTypeIDs());
    }
}