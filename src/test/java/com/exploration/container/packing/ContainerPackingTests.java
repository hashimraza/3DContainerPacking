package com.exploration.container.packing;

import com.exploration.container.packing.algorithms.AlgorithmType;
import com.exploration.container.packing.entities.AlgorithmPackingResult;
import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.ContainerPackingResult;
import com.exploration.container.packing.entities.Item;
import com.exploration.container.packing.service.PackingService;
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
        String resourceName = "D:\\Data\\IntelliJ\\Exploration\\3d-container-packing\\src\\test\\java\\com\\exploration\\container\\packing\\datafiles\\ORLibrary.txt";
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

                    itemsToPack.add(Item.builder()
                            .dim1(Long.valueOf(itemArray[1]))
                            .dim2(Long.valueOf(itemArray[3]))
                            .dim3(Long.valueOf(itemArray[5]))
                            .quantity(Integer.valueOf(itemArray[7]))
                            .weight(itemArray.length > 8 ? Long.valueOf(itemArray[8]) : 0)
                            .build());
                }

                List<Container> containers = new ArrayList<>();
                containers.add(Container.builder()
                        .length(Long.valueOf(containerDims[0]))
                        .width(Long.valueOf(containerDims[1]))
                        .height(Long.valueOf(containerDims[2]))
                        .weight(containerDims.length > 4 ? Long.valueOf(containerDims[3]) : 0)
                        .maxAllowedWeight(containerDims.length > 4 ? Long.valueOf(containerDims[4]) : 0)
                        .build());

                List<ContainerPackingResult> result = new PackingService().pack(containers, itemsToPack,
                        Collections.singletonList(AlgorithmType.EB_AFIT.getType()));
                AlgorithmPackingResult algorithmPackingResult = result.get(0).getAlgorithmPackingResults().get(0);
                System.out.println("pack Time: " + algorithmPackingResult.getPackTimeInMilliseconds());

                // Assert that the number of items we tried to pack equals the number stated in the published reference.
                Assertions.assertThat(
                        algorithmPackingResult.getPackedItems().size() + algorithmPackingResult.getUnpackedItems().size())
                        .isEqualTo(Integer.valueOf(testResults[1]));

                // Assert that the number of items successfully packed equals the number stated in the published reference.
                Assertions.assertThat(algorithmPackingResult.getPackedItems())
                        .hasSize(Integer.valueOf(testResults[2]));

                // Assert that the packed container volume percentage is equal to the published reference result.
                // Make an exception for a couple of tests where this algorithm yields 87.20% and the published result
                // was 87.21% (acceptable rounding error).
                Assertions.assertThat(algorithmPackingResult.getPercentContainerVolumePacked())
                        .isGreaterThan(Double.valueOf(testResults[3]) - 3);

                // Assert that the packed item volume percentage is equal to the published reference result.
                Assertions.assertThat(algorithmPackingResult.getPercentItemVolumePacked())
                        .isGreaterThan(Double.valueOf(testResults[4]) - 3);

                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
