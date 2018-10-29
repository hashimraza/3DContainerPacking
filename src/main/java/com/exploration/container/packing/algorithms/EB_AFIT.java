package com.exploration.container.packing.algorithms;

import com.exploration.container.packing.entities.AlgorithmPackingResult;
import com.exploration.container.packing.entities.Container;
import com.exploration.container.packing.entities.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A 3D bin packing algorithm originally ported from https://github.com/keremdemirer/3dbinpackingjs,
 * which itself was a JavaScript port of https://github.com/wknechtel/3d-bin-pack/, which is a C reconstruction
 * of a novel algorithm developed in a U.S. Air Force master's thesis by Erhan Baltacioglu in 2001.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class EB_AFIT implements PackingAlgorithm {

    private List<Item> itemsToPack;
    private ArrayList<Item> itemsPackedInOrder;
    private ScrapPad scrapFirst;
    private ScrapPad smallestZ;
    private boolean evened;
    private boolean hundredPercentPacked = false;
    private boolean layerDone;
    private boolean packing;
    private boolean packingBest = false;
    private boolean quit = false;
    private int bBoxI;
    private int bestIteration;
    private int bestVariant;
    private int boxI;
    private int cBoxI;
    private double bbfx;
    private double bbfy;
    private double bbfz;
    private double bBoxX;
    private double bBoxY;
    private double bBoxZ;
    private double bfX;
    private double bfY;
    private double bfZ;
    private double boxX;
    private double boxY;
    private double boxZ;
    private double cboxx;
    private double cboxy;
    private double cboxz;
    private double layerInLayer;
    private double layerThickness;
    private double lilz;
    private double packedVolume;
    private double packedY;
    private double prelayer;
    private double prepackedy;
    private double preremainpy;
    private double remainpy;
    private double remainpz;
    private double itemsToPackCount;
    private double totalItemVolume;
    private double totalContainerVolume;
    private long maxAllowedWeight;
    private long packedWeight;

    /**
     * Runs the packing algorithm.
     *
     * @param container The container to pack items into.
     * @param items     The items to pack.
     * @return The bin packing result.
     */
    public AlgorithmPackingResult run(Container container, List<Item> items) {
        initialize(container, items);
        executeIterations(container);
        report(container);

        AlgorithmPackingResult result = new AlgorithmPackingResult();
        result.setAlgorithmId(AlgorithmType.EB_AFIT.getType());
        result.setAlgorithmName(AlgorithmType.EB_AFIT.name());
        result.getUnpackedItems().addAll(itemsToPack.stream()
                .skip(1)
                .filter(item -> !item.isPacked())
                .collect(toList()));
        result.setPackedItems(itemsPackedInOrder);
        result.setCompletePack(result.getUnpackedItems().size() == 0);

        return result;
    }

    /**
     * Initializes everything.
     *
     * @param container - Container in which to pack
     * @param items     - items to pack
     */
    private void initialize(Container container, List<Item> items) {
        itemsToPack = new ArrayList<>();
        itemsPackedInOrder = new ArrayList<>();

        // The original code uses 1-based indexing everywhere. This fake entry is added to the beginning
        // of the list to make that possible.
        itemsToPack.add(new Item());

        itemsToPackCount = 0;

        items.forEach(item ->
                itemsToPackCount += Stream
                        .generate(() -> Item.builder()
                                .id(item.getId())
                                .dim1(item.getDim1())
                                .dim2(item.getDim2())
                                .dim3(item.getDim3())
                                .weight(item.getWeight())
                                .build())
                        .limit(item.getQuantity())
                        .map(itemsToPack::add)
                        .count());

        totalContainerVolume = container.getLength() * container.getHeight() * container.getWidth();
        maxAllowedWeight = container.getMaxAllowedWeight() - container.getWeight();
        totalItemVolume = itemsToPack.stream().mapToDouble(Item::getVolume).sum();

        scrapFirst = new ScrapPad();

        scrapFirst.setPre(null);
        scrapFirst.setPost(null);
        packingBest = false;
        hundredPercentPacked = false;
        quit = false;
    }

    /**
     * Executes the packing algorithm variants.
     */
    private void executeIterations(Container container) {
        int iterateLayer;
        int layersIndex;
        double bestVolume = 0.0D;
        List<Layer> layers;

        for (int containerOrientationVariant = 1; (containerOrientationVariant <= 6) && !quit; containerOrientationVariant++) {
            ContainerVariant containerVariant = getContainerVariant(containerOrientationVariant, container);
            layers = new ArrayList<>();
            layers.add(new Layer(0, -1));
            layers.addAll(getCandidateLayers(containerVariant));
            layers = layers.stream().sorted(Comparator.comparingDouble(Layer::getLayerEval)).collect(toList());

            for (layersIndex = 1; (layersIndex < layers.size()) && !quit; layersIndex++) {
                packedVolume = 0.0D;
                packedWeight = 0L;
                packedY = 0;
                packing = true;
                layerThickness = layers.get(layersIndex).getLayerDim();
                iterateLayer = layersIndex;
                remainpy = containerVariant.getY();
                remainpz = containerVariant.getZ();

                itemsToPack.forEach(item -> item.setPacked(false));

                do {
                    layerInLayer = 0;
                    layerDone = false;
                    packLayer(containerVariant);
                    packedY = packedY + layerThickness;
                    remainpy = containerVariant.getY() - packedY;

                    if (layerInLayer != 0 && !quit) {
                        prepackedy = packedY;
                        preremainpy = remainpy;
                        remainpy = layerThickness - prelayer;
                        packedY = packedY - layerThickness + prelayer;
                        remainpz = lilz;
                        layerThickness = layerInLayer;
                        layerDone = false;

                        packLayer(containerVariant);

                        packedY = prepackedy;
                        remainpy = preremainpy;
                        remainpz = containerVariant.getZ();
                    }

                    findLayer(containerVariant, remainpy);
                } while (packing && !quit);

                if ((packedVolume > bestVolume) && !quit) {
                    bestVolume = packedVolume;
                    bestVariant = containerOrientationVariant;
                    bestIteration = iterateLayer;
                }

                if (hundredPercentPacked) break;
            }

            if (hundredPercentPacked) break;

            if ((container.getLength() == container.getHeight()) && (container.getHeight() == container.getWidth())) {
                containerOrientationVariant = 6;
            }
        }
    }

    private ContainerVariant getContainerVariant(int containerOrientationVariant, Container container) {
        ContainerVariant containerVariant;
        switch (containerOrientationVariant) {
            case 1:
                containerVariant = new ContainerVariant(container.getLength(), container.getHeight(), container.getWidth());
                break;
            case 2:
                containerVariant = new ContainerVariant(container.getWidth(), container.getHeight(), container.getLength());
                break;
            case 3:
                containerVariant = new ContainerVariant(container.getWidth(), container.getLength(), container.getHeight());
                break;
            case 4:
                containerVariant = new ContainerVariant(container.getHeight(), container.getLength(), container.getWidth());
                break;
            case 5:
                containerVariant = new ContainerVariant(container.getLength(), container.getWidth(), container.getHeight());
                break;
            case 6:
                containerVariant = new ContainerVariant(container.getHeight(), container.getWidth(), container.getLength());
                break;
            default:
                throw new IllegalStateException();
        }

        return containerVariant;
    }

    /**
     * Analyzes each unpacked box to find the best fitting one to the empty space given.
     */
    private void analyzeBox(int boxIndex, double hmx, double hy, double hmy, double hz, double hmz, double dim1,
                            double dim2,
                            double dim3) {
        if (dim1 <= hmx && dim2 <= hmy && dim3 <= hmz) {
            if (dim2 <= hy) {
                if ((hy - dim2 < bfY)
                        || (hy - dim2 == bfY && hmx - dim1 < bfX)
                        || (hy - dim2 == bfY && hmx - dim1 == bfX && Math.abs(hz - dim3) < bfZ)) {
                    boxX = dim1;
                    boxY = dim2;
                    boxZ = dim3;
                    bfX = hmx - dim1;
                    bfY = hy - dim2;
                    bfZ = Math.abs(hz - dim3);
                    boxI = boxIndex;
                }
            } else {
                if ((dim2 - hy < bbfy)
                        || (dim2 - hy == bbfy && hmx - dim1 < bbfx)
                        || (dim2 - hy == bbfy && hmx - dim1 == bbfx && Math.abs(hz - dim3) < bbfz)) {
                    bBoxX = dim1;
                    bBoxY = dim2;
                    bBoxZ = dim3;
                    bbfx = hmx - dim1;
                    bbfy = dim2 - hy;
                    bbfz = Math.abs(hz - dim3);
                    bBoxI = boxIndex;
                }
            }
        }
    }

    /**
     * After finding each box, the candidate boxes and the condition of the layer are examined.
     */
    private void checkFound() {
        evened = false;

        if (boxI != 0) {
            cBoxI = boxI;
            cboxx = boxX;
            cboxy = boxY;
            cboxz = boxZ;
        } else {
            if ((bBoxI > 0) && (layerInLayer != 0 || (smallestZ.getPre() == null && smallestZ.getPost() == null))) {
                if (layerInLayer == 0) {
                    prelayer = layerThickness;
                    lilz = smallestZ.getCumZ();
                }

                cBoxI = bBoxI;
                cboxx = bBoxX;
                cboxy = bBoxY;
                cboxz = bBoxZ;
                layerInLayer = layerInLayer + bBoxY - layerThickness;
                layerThickness = bBoxY;
            } else {
                if (smallestZ.getPre() == null && smallestZ.getPost() == null) {
                    layerDone = true;
                } else {
                    evened = true;

                    if (smallestZ.getPre() == null) {
                        smallestZ.setCumX(smallestZ.getPost().getCumX());
                        smallestZ.setCumZ(smallestZ.getPost().getCumZ());
                        smallestZ.setPost(smallestZ.getPost().getPost());
                        if (smallestZ.getPost() != null) {
                            smallestZ.getPost().setPre(smallestZ);
                        }
                    } else if (smallestZ.getPost() == null) {
                        smallestZ.getPre().setPost(null);
                        smallestZ.getPre().setCumX(smallestZ.getCumX());
                    } else {
                        if (smallestZ.getPre().getCumZ() == smallestZ.getPost().getCumZ()) {
                            smallestZ.getPre().setPost(smallestZ.getPost().getPost());

                            if (smallestZ.getPost().getPost() != null) {
                                smallestZ.getPost().getPost().setPre(smallestZ.getPre());
                            }

                            smallestZ.getPre().setCumX(smallestZ.getPost().getCumX());
                        } else {
                            smallestZ.getPre().setPost(smallestZ.getPost());
                            smallestZ.getPost().setPre(smallestZ.getPre());

                            if (smallestZ.getPre().getCumZ() < smallestZ.getPost().getCumZ()) {
                                smallestZ.getPre().setCumX(smallestZ.getCumX());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the most proper boxes by looking at all six possible orientations,
     * empty space given, adjacent boxes, and pallet limits.
     */
    private void findBox(double hmx, double hy, double hmy, double hz, double hmz) {
        bfX = 32767;
        bfY = 32767;
        bfZ = 32767;
        bbfx = 32767;
        bbfy = 32767;
        bbfz = 32767;
        boxI = 0;
        bBoxI = 0;

//        int x=0;
        for (int y = 1; y <= itemsToPackCount; y++) {

            if (itemsToPack.get(y).isPacked()) continue;

            if (y > itemsToPackCount) return;

            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim1(), itemsToPack.get(y).getDim2(),
                    itemsToPack.get(y).getDim3());

            if ((itemsToPack.get(y).getDim1() == itemsToPack.get(y).getDim3())
                    && (itemsToPack.get(y).getDim3() == itemsToPack.get(y).getDim2()))
                continue;

            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim1(), itemsToPack.get(y).getDim3(),
                    itemsToPack.get(y).getDim2());
            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim2(), itemsToPack.get(y).getDim1(),
                    itemsToPack.get(y).getDim3());
            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim2(), itemsToPack.get(y).getDim3(),
                    itemsToPack.get(y).getDim1());
            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim3(), itemsToPack.get(y).getDim1(),
                    itemsToPack.get(y).getDim2());
            analyzeBox(y, hmx, hy, hmy, hz, hmz, itemsToPack.get(y).getDim3(), itemsToPack.get(y).getDim2(),
                    itemsToPack.get(y).getDim1());
        }
    }

    /**
     * Finds the most proper layer height by looking at the unpacked boxes and the remaining empty space available.
     *
     * @param containerVariant - Container Variant to use
     * @param thickness        - layer thickness
     */
    private void findLayer(ContainerVariant containerVariant, double thickness) {
        double exDim = 0;
        double dimDiff;
        double dim2 = 0;
        double dim3 = 0;
        int y;
        int z;
        double layerEval;
        double eval;
        layerThickness = 0;
        eval = 1000000;

        for (int x = 1; x <= itemsToPackCount; x++) {
            if (itemsToPack.get(x).isPacked()) continue;

            for (y = 1; y <= 3; y++) {
                switch (y) {
                    case 1:
                        exDim = itemsToPack.get(x).getDim1();
                        dim2 = itemsToPack.get(x).getDim2();
                        dim3 = itemsToPack.get(x).getDim3();
                        break;

                    case 2:
                        exDim = itemsToPack.get(x).getDim2();
                        dim2 = itemsToPack.get(x).getDim1();
                        dim3 = itemsToPack.get(x).getDim3();
                        break;

                    case 3:
                        exDim = itemsToPack.get(x).getDim3();
                        dim2 = itemsToPack.get(x).getDim1();
                        dim3 = itemsToPack.get(x).getDim2();
                        break;
                }

                layerEval = 0;

                if ((exDim <= thickness)
                        && (((dim2 <= containerVariant.getX()) && (dim3 <= containerVariant.getZ()))
                        || ((dim3 <= containerVariant.getX()) && (dim2 <= containerVariant.getZ())))) {
                    for (z = 1; z <= itemsToPackCount; z++) {
                        if (!(x == z) && !(itemsToPack.get(z).isPacked())) {
                            dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim1());

                            if (Math.abs(exDim - itemsToPack.get(z).getDim2()) < dimDiff) {
                                dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim2());
                            }

                            if (Math.abs(exDim - itemsToPack.get(z).getDim3()) < dimDiff) {
                                dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim3());
                            }

                            layerEval = layerEval + dimDiff;
                        }
                    }

                    if (layerEval < eval) {
                        eval = layerEval;
                        layerThickness = exDim;
                    }
                }
            }
        }

        if (layerThickness == 0 || layerThickness > remainpy) packing = false;
    }

    /**
     * Finds the first to be packed gap in the layer edge.
     */
    private void findSmallestZ() {
        ScrapPad scrapmemb = scrapFirst;
        smallestZ = scrapmemb;

        while (scrapmemb.getPost() != null) {
            if (scrapmemb.getPost().getCumZ() < smallestZ.getCumZ()) {
                smallestZ = scrapmemb.getPost();
            }

            scrapmemb = scrapmemb.getPost();
        }
    }

    /**
     * Lists all possible layer heights by giving a weight value to each of them.
     *
     * @param containerVariant - Container variant to use
     */
    private List<Layer> getCandidateLayers(ContainerVariant containerVariant) {
        List<Layer> layers = new ArrayList<>();
        boolean same;
        double exDim = 0;
        double dimDiff;
        double dim2 = 0;
        double dim3 = 0;

        for (int x = 1; x <= itemsToPackCount; x++) {
            for (int y = 1; y <= 3; y++) {
                switch (y) {
                    case 1:
                        exDim = itemsToPack.get(x).getDim1();
                        dim2 = itemsToPack.get(x).getDim2();
                        dim3 = itemsToPack.get(x).getDim3();
                        break;

                    case 2:
                        exDim = itemsToPack.get(x).getDim2();
                        dim2 = itemsToPack.get(x).getDim1();
                        dim3 = itemsToPack.get(x).getDim3();
                        break;

                    case 3:
                        exDim = itemsToPack.get(x).getDim3();
                        dim2 = itemsToPack.get(x).getDim1();
                        dim3 = itemsToPack.get(x).getDim2();
                        break;
                }

                if ((exDim > containerVariant.getY())
                        || (dim2 > containerVariant.getX()
                        || (dim3 > containerVariant.getZ() && (dim3 > containerVariant.getX() || dim2 > containerVariant.getZ()))))
                    continue;

                same = false;

                for (int k = 1; k <= layers.size() - 1; k++) {
                    if (exDim == layers.get(k).getLayerDim()) {
                        same = true;
                    }
                }

                if (same) continue;

                double layerEval = 0;

                for (int z = 1; z <= itemsToPackCount; z++) {
                    if (!(x == z)) {
                        dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim1());

                        if (Math.abs(exDim - itemsToPack.get(z).getDim2()) < dimDiff) {
                            dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim2());
                        }
                        if (Math.abs(exDim - itemsToPack.get(z).getDim3()) < dimDiff) {
                            dimDiff = Math.abs(exDim - itemsToPack.get(z).getDim3());
                        }
                        layerEval = layerEval + dimDiff;
                    }
                }

                layers.add(new Layer(exDim, layerEval));
            }
        }

        return layers;
    }

    /**
     * Transforms the found coordinate system to the one entered by the user and writes them
     * to the report file.
     */
    private void outputBoxList() {
        double packCoordX = 0;
        double packCoordY = 0;
        double packCoordZ = 0;
        double packDimX = 0;
        double packDimY = 0;
        double packDimZ = 0;

        switch (bestVariant) {
            case 1:
                packCoordX = itemsToPack.get(cBoxI).getCoordX();
                packCoordY = itemsToPack.get(cBoxI).getCoordY();
                packCoordZ = itemsToPack.get(cBoxI).getCoordZ();
                packDimX = itemsToPack.get(cBoxI).getPackDimX();
                packDimY = itemsToPack.get(cBoxI).getPackDimY();
                packDimZ = itemsToPack.get(cBoxI).getPackDimZ();
                break;

            case 2:
                packCoordX = itemsToPack.get(cBoxI).getCoordZ();
                packCoordY = itemsToPack.get(cBoxI).getCoordY();
                packCoordZ = itemsToPack.get(cBoxI).getCoordX();
                packDimX = itemsToPack.get(cBoxI).getPackDimZ();
                packDimY = itemsToPack.get(cBoxI).getPackDimY();
                packDimZ = itemsToPack.get(cBoxI).getPackDimX();
                break;

            case 3:
                packCoordX = itemsToPack.get(cBoxI).getCoordY();
                packCoordY = itemsToPack.get(cBoxI).getCoordZ();
                packCoordZ = itemsToPack.get(cBoxI).getCoordX();
                packDimX = itemsToPack.get(cBoxI).getPackDimY();
                packDimY = itemsToPack.get(cBoxI).getPackDimZ();
                packDimZ = itemsToPack.get(cBoxI).getPackDimX();
                break;

            case 4:
                packCoordX = itemsToPack.get(cBoxI).getCoordY();
                packCoordY = itemsToPack.get(cBoxI).getCoordX();
                packCoordZ = itemsToPack.get(cBoxI).getCoordZ();
                packDimX = itemsToPack.get(cBoxI).getPackDimY();
                packDimY = itemsToPack.get(cBoxI).getPackDimX();
                packDimZ = itemsToPack.get(cBoxI).getPackDimZ();
                break;

            case 5:
                packCoordX = itemsToPack.get(cBoxI).getCoordX();
                packCoordY = itemsToPack.get(cBoxI).getCoordZ();
                packCoordZ = itemsToPack.get(cBoxI).getCoordY();
                packDimX = itemsToPack.get(cBoxI).getPackDimX();
                packDimY = itemsToPack.get(cBoxI).getPackDimZ();
                packDimZ = itemsToPack.get(cBoxI).getPackDimY();
                break;

            case 6:
                packCoordX = itemsToPack.get(cBoxI).getCoordZ();
                packCoordY = itemsToPack.get(cBoxI).getCoordX();
                packCoordZ = itemsToPack.get(cBoxI).getCoordY();
                packDimX = itemsToPack.get(cBoxI).getPackDimZ();
                packDimY = itemsToPack.get(cBoxI).getPackDimX();
                packDimZ = itemsToPack.get(cBoxI).getPackDimY();
                break;
        }

        itemsToPack.get(cBoxI).setCoordX(packCoordX);
        itemsToPack.get(cBoxI).setCoordY(packCoordY);
        itemsToPack.get(cBoxI).setCoordZ(packCoordZ);
        itemsToPack.get(cBoxI).setPackDimX(packDimX);
        itemsToPack.get(cBoxI).setPackDimY(packDimY);
        itemsToPack.get(cBoxI).setPackDimZ(packDimZ);

        itemsPackedInOrder.add(itemsToPack.get(cBoxI));
    }

    /**
     * Packs the boxes found and arranges all variables and records properly.
     *
     * @param containerVariant - Container variant to use
     */
    private void packLayer(ContainerVariant containerVariant) {
        double lenX;
        double lenZ;
        double lpz;

        if (layerThickness == 0) {
            packing = false;
            return;
        }

        scrapFirst.setCumX(containerVariant.getX());
        scrapFirst.setCumZ(0);

        for (; !quit; ) {
            findSmallestZ();

            if ((smallestZ.getPre() == null) && (smallestZ.getPost() == null)) {
                //*** SITUATION-1: NO BOXES ON THE RIGHT AND LEFT SIDES ***

                lenX = smallestZ.getCumX();
                lpz = remainpz - smallestZ.getCumZ();
                findBox(lenX, layerThickness, remainpy, lpz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordX(0);
                itemsToPack.get(cBoxI).setCoordY(packedY);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.getCumZ());
                if (cboxx == smallestZ.getCumX()) {
                    smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                } else {
                    smallestZ.setPost(new ScrapPad());

                    smallestZ.getPost().setPost(null);
                    smallestZ.getPost().setPre(smallestZ);
                    smallestZ.getPost().setCumX(smallestZ.getCumX());
                    smallestZ.getPost().setCumZ(smallestZ.getCumZ());
                    smallestZ.setCumX(cboxx);
                    smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                }
            } else if (smallestZ.getPre() == null) {
                //*** SITUATION-2: NO BOXES ON THE LEFT SIDE ***

                lenX = smallestZ.getCumX();
                lenZ = smallestZ.getPost().getCumZ() - smallestZ.getCumZ();
                lpz = remainpz - smallestZ.getCumZ();
                findBox(lenX, layerThickness, remainpy, lenZ, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedY);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.getCumZ());
                if (cboxx == smallestZ.getCumX()) {
                    itemsToPack.get(cBoxI).setCoordX(0);

                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPost().getCumZ()) {
                        smallestZ.setCumZ(smallestZ.getPost().getCumZ());
                        smallestZ.setCumX(smallestZ.getPost().getCumX());
                        smallestZ.setPost(smallestZ.getPost().getPost());

                        if (smallestZ.getPost() != null) {
                            smallestZ.getPost().setPre(smallestZ);
                        }
                    } else {
                        smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                } else {
                    itemsToPack.get(cBoxI).setCoordX(smallestZ.getCumX() - cboxx);

                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPost().getCumZ()) {
                        smallestZ.setCumX(smallestZ.getCumX() - cboxx);
                    } else {
                        smallestZ.getPost().setPre(new ScrapPad());

                        smallestZ.getPost().getPre().setPost(smallestZ.getPost());
                        smallestZ.getPost().getPre().setPre(smallestZ);
                        smallestZ.setPost(smallestZ.getPost().getPre());
                        smallestZ.getPost().setCumX(smallestZ.getCumX());
                        smallestZ.setCumX(smallestZ.getCumX() - cboxx);
                        smallestZ.getPost().setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                }
            } else if (smallestZ.getPost() == null) {
                //*** SITUATION-3: NO BOXES ON THE RIGHT SIDE ***

                lenX = smallestZ.getCumX() - smallestZ.getPre().getCumX();
                lenZ = smallestZ.getPre().getCumZ() - smallestZ.getCumZ();
                lpz = remainpz - smallestZ.getCumZ();
                findBox(lenX, layerThickness, remainpy, lenZ, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedY);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.getCumZ());
                itemsToPack.get(cBoxI).setCoordX(smallestZ.getPre().getCumX());

                if (cboxx == smallestZ.getCumX() - smallestZ.getPre().getCumX()) {
                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPre().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getCumX());
                        smallestZ.getPre().setPost(null);
                    } else {
                        smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                } else {
                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPre().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getPre().getCumX() + cboxx);
                    } else {
                        smallestZ.getPre().setPost(new ScrapPad());

                        smallestZ.getPre().getPost().setPre(smallestZ.getPre());
                        smallestZ.getPre().getPost().setPost(smallestZ);
                        smallestZ.setPre(smallestZ.getPre().getPost());
                        smallestZ.getPre().setCumX(smallestZ.getPre().getPre().getCumX() + cboxx);
                        smallestZ.getPre().setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                }
            } else if (smallestZ.getPre().getCumZ() == smallestZ.getPost().getCumZ()) {
                //*** SITUATION-4: THERE ARE BOXES ON BOTH OF THE SIDES ***

                //*** SUBSITUATION-4A: SIDES ARE EQUAL TO EACH OTHER ***

                lenX = smallestZ.getCumX() - smallestZ.getPre().getCumX();
                lenZ = smallestZ.getPre().getCumZ() - smallestZ.getCumZ();
                lpz = remainpz - smallestZ.getCumZ();

                findBox(lenX, layerThickness, remainpy, lenZ, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedY);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.getCumZ());

                if (cboxx == smallestZ.getCumX() - smallestZ.getPre().getCumX()) {
                    itemsToPack.get(cBoxI).setCoordX(smallestZ.getPre().getCumX());

                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPost().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getPost().getCumX());

                        if (smallestZ.getPost().getPost() != null) {
                            smallestZ.getPre().setPost(smallestZ.getPost().getPost());
                            smallestZ.getPost().getPost().setPre(smallestZ.getPre());
                        } else {
                            smallestZ.getPre().setPost(null);
                        }
                    } else {
                        smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                } else if (smallestZ.getPre().getCumX() < containerVariant.getX() - smallestZ.getCumX()) {
                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPre().getCumZ()) {
                        smallestZ.setCumX(smallestZ.getCumX() - cboxx);
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.getCumX() - cboxx);
                    } else {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.getPre().getCumX());
                        smallestZ.getPre().setPost(new ScrapPad());

                        smallestZ.getPre().getPost().setPre(smallestZ.getPre());
                        smallestZ.getPre().getPost().setPost(smallestZ);
                        smallestZ.setPre(smallestZ.getPre().getPost());
                        smallestZ.getPre().setCumX(smallestZ.getPre().getPre().getCumX() + cboxx);
                        smallestZ.getPre().setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                } else {
                    if (smallestZ.getCumZ() + cboxz == smallestZ.getPre().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getPre().getCumX() + cboxx);
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.getPre().getCumX());
                    } else {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.getCumX() - cboxx);
                        smallestZ.getPost().setPre(new ScrapPad());

                        smallestZ.getPost().getPre().setPost(smallestZ.getPost());
                        smallestZ.getPost().getPre().setPre(smallestZ);
                        smallestZ.setPost(smallestZ.getPost().getPre());
                        smallestZ.getPost().setCumX(smallestZ.getCumX());
                        smallestZ.getPost().setCumZ(smallestZ.getCumZ() + cboxz);
                        smallestZ.setCumX(smallestZ.getCumX() - cboxx);
                    }
                }
            } else {
                //*** SUBSITUATION-4B: SIDES ARE NOT EQUAL TO EACH OTHER ***

                lenX = smallestZ.getCumX() - smallestZ.getPre().getCumX();
                lenZ = smallestZ.getPre().getCumZ() - smallestZ.getCumZ();
                lpz = remainpz - smallestZ.getCumZ();
                findBox(lenX, layerThickness, remainpy, lenZ, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedY);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.getCumZ());
                itemsToPack.get(cBoxI).setCoordX(smallestZ.getPre().getCumX());

                if (cboxx == (smallestZ.getCumX() - smallestZ.getPre().getCumX())) {
                    if ((smallestZ.getCumZ() + cboxz) == smallestZ.getPre().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getCumX());
                        smallestZ.getPre().setPost(smallestZ.getPost());
                        smallestZ.getPost().setPre(smallestZ.getPre());
                    } else {
                        smallestZ.setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                } else {
                    if ((smallestZ.getCumZ() + cboxz) == smallestZ.getPre().getCumZ()) {
                        smallestZ.getPre().setCumX(smallestZ.getPre().getCumX() + cboxx);
                    } else if (smallestZ.getCumZ() + cboxz == smallestZ.getPost().getCumZ()) {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.getCumX() - cboxx);
                        smallestZ.setCumX(smallestZ.getCumX() - cboxx);
                    } else {
                        smallestZ.getPre().setPost(new ScrapPad());

                        smallestZ.getPre().getPost().setPre(smallestZ.getPre());
                        smallestZ.getPre().getPost().setPost(smallestZ);
                        smallestZ.setPre(smallestZ.getPre().getPost());
                        smallestZ.getPre().setCumX(smallestZ.getPre().getPre().getCumX() + cboxx);
                        smallestZ.getPre().setCumZ(smallestZ.getCumZ() + cboxz);
                    }
                }
            }

            volumeAndWeightCheck();
        }
    }

    /**
     * Using the parameters found, packs the best solution found and
     * reports to the console.
     */
    private void report(Container container) {
        quit = false;

        ContainerVariant containerVariant = getContainerVariant(bestVariant, container);

        packingBest = true;

        System.out.println("BEST SOLUTION FOUND AT ITERATION       :" + bestIteration + " OF VARIANT " + bestVariant);
        System.out.println("TOTAL ITEMS TO PACK                    :" + itemsToPackCount);
        System.out.println("TOTAL VOLUME OF ALL ITEMS              :" + totalItemVolume);
        System.out.println("WHILE CONTAINER ORIENTATION X - Y - Z  :" + containerVariant);

        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(0, -1));
        layers.addAll(getCandidateLayers(containerVariant));
        layers = layers.stream().sorted(Comparator.comparingDouble(Layer::getLayerEval)).collect(toList());
        packedVolume = 0D;
        packedWeight = 0L;
        packedY = 0;
        packing = true;
        layerThickness = layers.get(bestIteration).getLayerDim();
        remainpy = containerVariant.getY();
        remainpz = containerVariant.getZ();

        itemsToPack.forEach(item -> item.setPacked(false));

        do {
            layerInLayer = 0;
            layerDone = false;
            packLayer(containerVariant);
            packedY = packedY + layerThickness;
            remainpy = containerVariant.getY() - packedY;

            if (layerInLayer > 0.0001D) {
                prepackedy = packedY;
                preremainpy = remainpy;
                remainpy = layerThickness - prelayer;
                packedY = packedY - layerThickness + prelayer;
                remainpz = lilz;
                layerThickness = layerInLayer;
                layerDone = false;
                packLayer(containerVariant);
                packedY = prepackedy;
                remainpy = preremainpy;
                remainpz = containerVariant.getZ();
            }

            if (!quit) {
                findLayer(containerVariant, remainpy);
            }
        } while (packing && !quit);
    }

    /**
     * After packing of each item, the 100% packing condition is checked.
     */
    private void volumeAndWeightCheck() {
        Item itemToPack = itemsToPack.get(cBoxI);
        if ((packedVolume + itemToPack.getVolume()) <= totalContainerVolume
                && (packedVolume + itemToPack.getVolume()) <= totalItemVolume
                && (packedWeight + itemToPack.getWeight()) <= maxAllowedWeight) {
            itemToPack.setPacked(true);
            itemToPack.setPackDimX(cboxx);
            itemToPack.setPackDimY(cboxy);
            itemToPack.setPackDimZ(cboxz);
            packedVolume += itemToPack.getVolume();
            packedWeight += itemToPack.getWeight();

            if (packingBest) {
                outputBoxList();
            }
        } else {
            packing = false;
            hundredPercentPacked = true;
        }
    }


    /**
     * A list that stores all the different lengths of all item dimensions.
     * From the master's thesis:
     * "Each Layerdim value in this array represents a different layer thickness
     * value with which each iteration can start packing. Before starting iterations,
     * all different lengths of all box dimensions along with evaluation values are
     * stored in this array" (p. 3-6).
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class Layer {
        /**
         * Gets or sets the layer dimension value, representing a layer thickness.
         * The layer dimension value.
         */
        private double layerDim;
        /**
         * Gets or sets the layer eval value, representing an evaluation weight
         * value for the corresponding layerDim value.
         * The layer eval value.
         */
        private double layerEval;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class ContainerVariant {
        private double x;
        private double y;
        private double z;
    }

    /**
     * From the master's thesis:
     * "The double linked list we use keeps the topology of the edge of the
     * current layer under construction. We keep the x and z coordinates of
     * each gap's right corner. The program looks at those gaps and tries to
     * fill them with boxes one at a time while trying to keep the edge of the
     * layer even" (p. 3-7).
     */
    @Data
    private class ScrapPad {

        /**
         * Gets or sets the x coordinate of the gap's right corner.
         * The x coordinate of the gap's right corner.
         */
        private double cumX;

        /**
         * Gets or sets the z coordinate of the gap's right corner.
         * The z coordinate of the gap's right corner.
         */
        private double cumZ;

        /**
         * Gets or sets the following entry.
         * The following entry.
         */
        private ScrapPad post;
        /**
         * Gets or sets the previous entry.
         * The previous entry.
         */
        private ScrapPad pre;

    }
}