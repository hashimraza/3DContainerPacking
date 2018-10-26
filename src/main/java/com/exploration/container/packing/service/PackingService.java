package com.exploration.container.packing.service;

import com.exploration.container.packing.algorithms.AlgorithmType;
import com.exploration.container.packing.algorithms.EB_AFIT;
import com.exploration.container.packing.algorithms.PackingAlgorithm;
import com.exploration.container.packing.entities.AlgorithmPackingResult;
import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.ContainerPackingResult;
import com.exploration.container.packing.entities.Item;
import com.google.common.base.Stopwatch;

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
            containerPackingResult.setContainerId(container.getId());

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

                itemsToPack.forEach(item -> items.add(new Item(item.getId(), item.getDim1(), item.getDim2(), item.getDim3(),
                        item.getQuantity())));

                Stopwatch stopwatch = Stopwatch.createStarted();
                assert algorithm != null;
                AlgorithmPackingResult algorithmResult = algorithm.run(container, items);
                stopwatch.stop();

                algorithmResult.setPackTimeInMilliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS));

                double containerVolume = container.getLength() * container.getWidth() * container.getHeight();
                double itemVolumePacked = algorithmResult.getPackedItems().stream().mapToDouble(i -> i.getVolume()).sum();
                double itemVolumeUnpacked = algorithmResult.getUnpackedItems().stream().mapToDouble(i -> i.getVolume()).sum();

                algorithmResult.setPercentContainerVolumePacked(round(itemVolumePacked / containerVolume * 100));
                algorithmResult.setPercentItemVolumePacked(
                        round(itemVolumePacked / (itemVolumePacked + itemVolumeUnpacked) * 100));

                synchronized (sync) {
                    containerPackingResult.getAlgorithmPackingResults().add(algorithmResult);
                }
            });

            containerPackingResult.setAlgorithmPackingResults(containerPackingResult.getAlgorithmPackingResults()
                    .stream().sorted(Comparator.comparing(r -> r.getAlgorithmName())).collect(Collectors.toList()));

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
