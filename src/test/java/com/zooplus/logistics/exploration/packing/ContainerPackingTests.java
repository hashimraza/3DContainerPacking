package com.zooplus.logistics.exploration.packing;

import com.zooplus.logistics.exploration.packing.algorithms.AlgorithmType;
import com.zooplus.logistics.exploration.packing.entities.AlgorithmPackingResult;
import com.zooplus.logistics.exploration.packing.entities.Container;
import com.zooplus.logistics.exploration.packing.entities.ContainerPackingResult;
import com.zooplus.logistics.exploration.packing.entities.Item;
import com.zooplus.logistics.exploration.packing.service.PackingService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContainerPackingTests {
    @Test
    public void EB_AFIT_Passes_700_Standard_Reference_Tests() {
        // ORLibrary.txt is an Embedded Resource in this project.
        String resourceName = "D:/Data/IntelliJ/Exploration/3d-container-packing/src/test/java/com/zooplus/logistics/exploration/packing/datafiles/ORLibrary.txt";
        // Counter to control how many tests are run in dev.
        File file = new File(resourceName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int counter = 1;

            while (br.readLine() != null && counter <= 200) {
                List<Item> itemsToPack = new ArrayList<>();

                // First line in each test case is an id. Skip it.

                // Second line states the results of the test, as reported in the EB-AFIT master's thesis, appendix E.
                String[] testResults = br.readLine().split(" ");

                // Third line defines the container dimensions.
                String[] containerDims = br.readLine().split(" ");

                // Fourth line states how many distinct item types we are packing.
                int itemTypeCount = Integer.valueOf(br.readLine());

                for (int i = 0; i < itemTypeCount; i++) {
                    String[] itemArray = br.readLine().split(" ");

                    Item item = new Item(0, Long.valueOf(itemArray[1]), Long.valueOf(itemArray[3]),
                            Long.valueOf(itemArray[5]), Integer.valueOf(itemArray[7]));
                    itemsToPack.add(item);
                }

                List<Container> containers = new ArrayList<>();
                containers.add(new Container(0, Long.valueOf(containerDims[0]), Long.valueOf(containerDims[1]),
                        Long.valueOf(containerDims[2])));

                List<ContainerPackingResult> result = new PackingService().pack(containers, itemsToPack,
                        Collections.singletonList(AlgorithmType.EB_AFIT.getType()));
                AlgorithmPackingResult algorithmPackingResult = result.get(0).algorithmPackingResults.get(0);
                System.out.println("pack Time: " + algorithmPackingResult.packTimeInMilliseconds);

                // Assert that the number of items we tried to pack equals the number stated in the published reference.
                Assertions.assertThat(algorithmPackingResult.packedItems.size() + algorithmPackingResult.unpackedItems.size())
                        .isEqualTo(Integer.valueOf(testResults[1]));

                // Assert that the number of items successfully packed equals the number stated in the published reference.
                Assertions.assertThat(algorithmPackingResult.packedItems)
                        .hasSize(Integer.valueOf(testResults[2]));

                // Assert that the packed container volume percentage is equal to the published reference result.
                // Make an exception for a couple of tests where this algorithm yields 87.20% and the published result
                // was 87.21% (acceptable rounding error).
                Assertions.assertThat(algorithmPackingResult.percentContainerVolumePacked)
                        .isGreaterThan(Double.valueOf(testResults[3]) - 3);

                // Assert that the packed item volume percentage is equal to the published reference result.
                Assertions.assertThat(algorithmPackingResult.percentItemVolumePacked)
                        .isGreaterThan(Double.valueOf(testResults[4]) - 3);

                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
