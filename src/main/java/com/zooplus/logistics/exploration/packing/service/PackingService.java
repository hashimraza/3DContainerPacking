package com.zooplus.logistics.exploration.packing.service;

import com.google.common.base.Stopwatch;
import com.zooplus.logistics.exploration.packing.algorithms.AlgorithmType;
import com.zooplus.logistics.exploration.packing.algorithms.EB_AFIT;
import com.zooplus.logistics.exploration.packing.algorithms.PackingAlgorithm;
import com.zooplus.logistics.exploration.packing.entities.AlgorithmPackingResult;
import com.zooplus.logistics.exploration.packing.entities.Container;
import com.zooplus.logistics.exploration.packing.entities.ContainerPackingResult;
import com.zooplus.logistics.exploration.packing.entities.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.round;


/// <summary>
/// The container packing service.
/// </summary>
public class PackingService {
    /// <summary>
    /// Attempts to pack the specified containers with the specified items import the specified algorithms.
    /// </summary>
    /// <param name="containers">The list of containers to pack.</param>
    /// <param name="itemsToPack">The items to pack.</param>
    /// <param name="algorithmTypeIDs">The list of algorithm type IDs to use for packing.</param>
    /// <returns>A container packing result with lists of the packed and unpacked items.</returns>
    public List<ContainerPackingResult> pack(List<Container> containers, List<Item> itemsToPack, List<Integer> algorithmTypeIDs) {
        Object sync = new Object();
        List<ContainerPackingResult> result = new ArrayList<>();

        containers.forEach(container ->
        {
            ContainerPackingResult containerPackingResult = new ContainerPackingResult();
            containerPackingResult.containerId = container.id;

            algorithmTypeIDs.parallelStream().forEach(algorithmTypeID ->
            {
                PackingAlgorithm algorithm = null;
                try {
                    algorithm = getPackingAlgorithmFromTypeID(algorithmTypeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Until I rewrite the algorithm with no side effects, we need to clone the item list
                // so the parallel updates don't interfere with each other.
                ArrayList<Item> items = new ArrayList<>();

                itemsToPack.forEach(item -> items.add(new Item(item.id, item.dim1, item.dim2, item.dim3, item.quantity)));

                Stopwatch stopwatch = Stopwatch.createStarted();
                assert algorithm != null;
                AlgorithmPackingResult algorithmResult = algorithm.run(container, items);
                stopwatch.stop();

                algorithmResult.packTimeInMilliseconds = stopwatch.elapsed(TimeUnit.MILLISECONDS);

                double containerVolume = container.length * container.width * container.height;
                double itemVolumePacked = algorithmResult.packedItems.stream().mapToDouble(i -> i.volume).sum();
                double itemVolumeUnpacked = algorithmResult.unpackedItems.stream().mapToDouble(i -> i.volume).sum();

                algorithmResult.percentContainerVolumePacked = round(itemVolumePacked / containerVolume * 100);
                algorithmResult.percentItemVolumePacked = round(itemVolumePacked / (itemVolumePacked + itemVolumeUnpacked) * 100);

                synchronized (sync) {
                    containerPackingResult.algorithmPackingResults.add(algorithmResult);
                }
            });

            containerPackingResult.algorithmPackingResults = containerPackingResult.algorithmPackingResults
                    .stream().sorted(Comparator.comparing(r -> r.algorithmName)).collect(Collectors.toList());

            synchronized (sync) {
                result.add(containerPackingResult);
            }
        });

        return result;
    }

    private PackingAlgorithm getPackingAlgorithmFromTypeID(int algorithmTypeID) throws Exception {
        switch (AlgorithmType.findByType(algorithmTypeID)) {
            case EB_AFIT:
                return new EB_AFIT();
            default:
                throw new Exception("Invalid algorithm type.");
        }
    }
}
