package com.exploration.container.packing.service;

import com.exploration.container.packing.algorithms.AlgorithmType;
import com.exploration.container.packing.algorithms.EB_AFIT;
import com.exploration.container.packing.algorithms.PackingAlgorithm;
import com.exploration.container.packing.entities.AlgorithmPackingResult;
import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.ContainerPackingResult;
import com.exploration.container.packing.entities.Item;
import com.google.common.base.Stopwatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.round;

@Service
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
                    algorithm = getPackingAlgorithmFromTypeId(algorithmTypeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Until I rewrite the algorithm with no side effects, we need to clone the item list
                // so the parallel updates don't interfere with each other.
                ArrayList<Item> items = new ArrayList<>();

                itemsToPack.forEach(item -> items.add(Item.builder()
                        .id(item.getId())
                        .dim1(item.getDim1())
                        .dim2(item.getDim2())
                        .dim3(item.getDim3())
                        .quantity(item.getQuantity())
                        .weight(item.getWeight())
                        .build()));

                Stopwatch stopwatch = Stopwatch.createStarted();
                assert algorithm != null;
                AlgorithmPackingResult algorithmResult = algorithm.run(container, items);
                stopwatch.stop();

                algorithmResult.setPackTimeInMilliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS));

                double containerVolume = container.getLength() * container.getWidth() * container.getHeight();
                long containerWeight = container.getWeight();
                double itemVolumePacked = algorithmResult.getPackedItems().stream().mapToDouble(Item::getVolume).sum();
                double itemVolumeUnpacked = algorithmResult.getUnpackedItems().stream().mapToDouble(Item::getVolume).sum();
                double itemWeightPacked = algorithmResult.getPackedItems().stream().mapToDouble(Item::getWeight).sum();
                double itemWeightUnpacked = algorithmResult.getUnpackedItems().stream().mapToDouble(Item::getWeight).sum();

                algorithmResult.setPercentContainerVolumePacked(round(itemVolumePacked / containerVolume * 100));
                algorithmResult.setPercentItemVolumePacked(
                        round(itemVolumePacked / (itemVolumePacked + itemVolumeUnpacked) * 100));
                algorithmResult.setPercentWeightPacked(round((itemWeightPacked + containerWeight)
                        / (itemWeightPacked + itemWeightUnpacked + containerWeight) * 100));


                synchronized (sync) {
                    containerPackingResult.getAlgorithmPackingResults().add(algorithmResult);
                }
            });

            containerPackingResult.setAlgorithmPackingResults(containerPackingResult.getAlgorithmPackingResults()
                    .stream()
                    .sorted(Comparator.comparing(AlgorithmPackingResult::getAlgorithmName))
                    .collect(Collectors.toList()));

            synchronized (sync) {
                result.add(containerPackingResult);
            }
        });

        return result;
    }

    private PackingAlgorithm getPackingAlgorithmFromTypeId(int algorithmTypeId) throws Exception {
        switch (AlgorithmType.findByType(algorithmTypeId)) {
            case EB_AFIT:
                return new EB_AFIT();
            default:
                throw new Exception("Invalid algorithm type.");
        }
    }
}
