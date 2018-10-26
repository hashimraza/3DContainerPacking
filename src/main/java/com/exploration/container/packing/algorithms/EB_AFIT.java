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
import java.util.stream.Collectors;

/// A 3D bin packing algorithm originally ported from https://github.com/keremdemirer/3dbinpackingjs,
/// which itself was a JavaScript port of https://github.com/wknechtel/3d-bin-pack/, which is a C reconstruction
/// of a novel algorithm developed in a U.S. Air Force master's thesis by Erhan Baltacioglu in 2001.
/// </summary>
@SuppressWarnings("SuspiciousNameCombination")
public class EB_AFIT implements PackingAlgorithm {

    private List<Item> itemsToPack;
    private ArrayList<Item> itemsPackedInOrder;
    private List<Layer> layers;
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
    private int layerListLen;
    private int x;
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
    private double layerinlayer;
    private double layerThickness;
    private double lilz;
    private double packedVolume;
    private double packedy;
    private double prelayer;
    private double prepackedy;
    private double preremainpy;
    private double px;
    private double py;
    private double pz;
    private double remainpy;
    private double remainpz;
    private double itemsToPackCount;
    private double totalItemVolume;
    private double totalContainerVolume;

    /// <summary>
/// Runs the packing algorithm.
/// </summary>
/// <param name="container">The container to pack items into.</param>
/// <param name="items">The items to pack.</param>
/// <returns>The bin packing result.</returns>

    public AlgorithmPackingResult run(Container container, List<Item> items) {
        initialize(container, items);
        executeIterations(container);
        report(container);

        AlgorithmPackingResult result = new AlgorithmPackingResult();
        result.setAlgorithmId(AlgorithmType.EB_AFIT.getType());
        result.setAlgorithmName(AlgorithmType.EB_AFIT.name());

        for (int i = 1; i <= itemsToPackCount; i++) {
            itemsToPack.get(i).setQuantity(1);

            if (!itemsToPack.get(i).isPacked()) {
                result.getUnpackedItems().add(itemsToPack.get(i));
            }
        }

        result.setPackedItems(itemsPackedInOrder);


        if (result.getUnpackedItems().size() == 0) {
            result.setCompletePack(true);
        }

        return result;
    }

    /// <summary>
/// Analyzes each unpacked box to find the best fitting one to the empty space given.
/// </summary>
    private void analyzeBox(double hmx, double hy, double hmy, double hz, double hmz, double dim1, double dim2, double dim3) {
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
                    boxI = x;
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
                    bBoxI = x;
                }
            }
        }
    }

    /// <summary>
/// After finding each box, the candidate boxes and the condition of the layer are examined.
/// </summary>
    private void checkFound() {
        evened = false;

        if (boxI != 0) {
            cBoxI = boxI;
            cboxx = boxX;
            cboxy = boxY;
            cboxz = boxZ;
        } else {
            if ((bBoxI > 0) && (layerinlayer != 0 || (smallestZ.pre == null && smallestZ.post == null))) {
                if (layerinlayer == 0) {
                    prelayer = layerThickness;
                    lilz = smallestZ.cumZ;
                }

                cBoxI = bBoxI;
                cboxx = bBoxX;
                cboxy = bBoxY;
                cboxz = bBoxZ;
                layerinlayer = layerinlayer + bBoxY - layerThickness;
                layerThickness = bBoxY;
            } else {
                if (smallestZ.pre == null && smallestZ.post == null) {
                    layerDone = true;
                } else {
                    evened = true;

                    if (smallestZ.pre == null) {
                        smallestZ.cumX = smallestZ.post.cumX;
                        smallestZ.cumZ = smallestZ.post.cumZ;
                        smallestZ.post = smallestZ.post.post;
                        if (smallestZ.post != null) {
                            smallestZ.post.pre = smallestZ;
                        }
                    } else if (smallestZ.post == null) {
                        smallestZ.pre.post = null;
                        smallestZ.pre.cumX = smallestZ.cumX;
                    } else {
                        if (smallestZ.pre.cumZ == smallestZ.post.cumZ) {
                            smallestZ.pre.post = smallestZ.post.post;

                            if (smallestZ.post.post != null) {
                                smallestZ.post.post.pre = smallestZ.pre;
                            }

                            smallestZ.pre.cumX = smallestZ.post.cumX;
                        } else {
                            smallestZ.pre.post = smallestZ.post;
                            smallestZ.post.pre = smallestZ.pre;

                            if (smallestZ.pre.cumZ < smallestZ.post.cumZ) {
                                smallestZ.pre.cumX = smallestZ.cumX;
                            }
                        }
                    }
                }
            }
        }
    }

    /// <summary>
/// Executes the packing algorithm variants.
/// </summary>
    private void executeIterations(Container container) {
        int itelayer;
        int layersIndex;
        double bestVolume = 0.0D;

        for (int containerOrientationVariant = 1; (containerOrientationVariant <= 6) && !quit; containerOrientationVariant++) {
            switch (containerOrientationVariant) {
                case 1:
                    px = container.getLength();
                    py = container.getHeight();
                    pz = container.getWidth();
                    break;

                case 2:
                    px = container.getWidth();
                    py = container.getHeight();
                    pz = container.getLength();
                    break;

                case 3:
                    px = container.getWidth();
                    py = container.getLength();
                    pz = container.getHeight();
                    break;

                case 4:
                    px = container.getHeight();
                    py = container.getLength();
                    pz = container.getWidth();
                    break;

                case 5:
                    px = container.getLength();
                    py = container.getWidth();
                    pz = container.getHeight();
                    break;

                case 6:
                    px = container.getHeight();
                    py = container.getWidth();
                    pz = container.getLength();
                    break;
            }

            layers.add(new Layer(0, -1));
            listCanditLayers();
            layers = layers.stream().sorted(Comparator.comparingDouble(l -> l.layerEval)).collect(Collectors.toList());

            for (layersIndex = 1; (layersIndex <= layerListLen) && !quit; layersIndex++) {
                packedVolume = 0.0D;
                packedy = 0;
                packing = true;
                layerThickness = layers.get(layersIndex).layerDim;
                itelayer = layersIndex;
                remainpy = py;
                remainpz = pz;

                for (x = 1; x <= itemsToPackCount; x++) {
                    itemsToPack.get(x).setPacked(false);
                }

                do {
                    layerinlayer = 0;
                    layerDone = false;

                    packLayer();

                    packedy = packedy + layerThickness;
                    remainpy = py - packedy;

                    if (layerinlayer != 0 && !quit) {
                        prepackedy = packedy;
                        preremainpy = remainpy;
                        remainpy = layerThickness - prelayer;
                        packedy = packedy - layerThickness + prelayer;
                        remainpz = lilz;
                        layerThickness = layerinlayer;
                        layerDone = false;

                        packLayer();

                        packedy = prepackedy;
                        remainpy = preremainpy;
                        remainpz = pz;
                    }

                    findLayer(remainpy);
                } while (packing && !quit);

                if ((packedVolume > bestVolume) && !quit) {
                    bestVolume = packedVolume;
                    bestVariant = containerOrientationVariant;
                    bestIteration = itelayer;
                }

                if (hundredPercentPacked) break;
            }

            if (hundredPercentPacked) break;

            if ((container.getLength() == container.getHeight()) && (container.getHeight() == container.getWidth()))
                containerOrientationVariant = 6;

            layers = new ArrayList<>();
        }
    }

    /// <summary>
/// Finds the most proper boxes by looking at all six possible orientations,
/// empty space given, adjacent boxes, and pallet limits.
/// </summary>
    private void findBox(double hmx, double hy, double hmy, double hz, double hmz) {
        int y;
        bfX = 32767;
        bfY = 32767;
        bfZ = 32767;
        bbfx = 32767;
        bbfy = 32767;
        bbfz = 32767;
        boxI = 0;
        bBoxI = 0;

//        int x=0;
        for (y = 1; y <= itemsToPackCount; y = y + itemsToPack.get(y).getQuantity()) {
            for (x = y; x < x + itemsToPack.get(y).getQuantity() - 1; x++) {
                if (!itemsToPack.get(x).isPacked()) break;
            }

            if (itemsToPack.get(x).isPacked()) continue;

            if (x > itemsToPackCount) return;

            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim1(), itemsToPack.get(x).getDim2(),
                    itemsToPack.get(x).getDim3());

            if ((itemsToPack.get(x).getDim1() == itemsToPack.get(x).getDim3()) && (itemsToPack.get(
                    x).getDim3() == itemsToPack.get(x).getDim2()))
                continue;

            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim1(), itemsToPack.get(x).getDim3(),
                    itemsToPack.get(x).getDim2());
            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim2(), itemsToPack.get(x).getDim1(),
                    itemsToPack.get(x).getDim3());
            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim2(), itemsToPack.get(x).getDim3(),
                    itemsToPack.get(x).getDim1());
            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim3(), itemsToPack.get(x).getDim1(),
                    itemsToPack.get(x).getDim2());
            analyzeBox(hmx, hy, hmy, hz, hmz, itemsToPack.get(x).getDim3(), itemsToPack.get(x).getDim2(),
                    itemsToPack.get(x).getDim1());
        }
    }

    /**
     * Finds the most proper layer height by looking at the unpacked boxes and the remaining empty space available.
     *
     * @param thickness - layer thickness
     */
    private void findLayer(double thickness) {
        double exdim = 0;
        double dimdif;
        double dimen2 = 0;
        double dimen3 = 0;
        int y;
        int z;
        double layereval;
        double eval;
        layerThickness = 0;
        eval = 1000000;

        for (x = 1; x <= itemsToPackCount; x++) {
            if (itemsToPack.get(x).isPacked()) continue;

            for (y = 1; y <= 3; y++) {
                switch (y) {
                    case 1:
                        exdim = itemsToPack.get(x).getDim1();
                        dimen2 = itemsToPack.get(x).getDim2();
                        dimen3 = itemsToPack.get(x).getDim3();
                        break;

                    case 2:
                        exdim = itemsToPack.get(x).getDim2();
                        dimen2 = itemsToPack.get(x).getDim1();
                        dimen3 = itemsToPack.get(x).getDim3();
                        break;

                    case 3:
                        exdim = itemsToPack.get(x).getDim3();
                        dimen2 = itemsToPack.get(x).getDim1();
                        dimen3 = itemsToPack.get(x).getDim2();
                        break;
                }

                layereval = 0;

                if ((exdim <= thickness) && (((dimen2 <= px) && (dimen3 <= pz)) || ((dimen3 <= px) && (dimen2 <= pz)))) {
                    for (z = 1; z <= itemsToPackCount; z++) {
                        if (!(x == z) && !(itemsToPack.get(z).isPacked())) {
                            dimdif = Math.abs(exdim - itemsToPack.get(z).getDim1());

                            if (Math.abs(exdim - itemsToPack.get(z).getDim2()) < dimdif) {
                                dimdif = Math.abs(exdim - itemsToPack.get(z).getDim2());
                            }

                            if (Math.abs(exdim - itemsToPack.get(z).getDim3()) < dimdif) {
                                dimdif = Math.abs(exdim - itemsToPack.get(z).getDim3());
                            }

                            layereval = layereval + dimdif;
                        }
                    }

                    if (layereval < eval) {
                        eval = layereval;
                        layerThickness = exdim;
                    }
                }
            }
        }

        if (layerThickness == 0 || layerThickness > remainpy) packing = false;
    }

    /// <summary>
/// Finds the first to be packed gap in the layer edge.
/// </summary>
    private void findSmallestZ() {
        ScrapPad scrapmemb = scrapFirst;
        smallestZ = scrapmemb;

        while (scrapmemb.post != null) {
            if (scrapmemb.post.cumZ < smallestZ.cumZ) {
                smallestZ = scrapmemb.post;
            }

            scrapmemb = scrapmemb.post;
        }
    }

    /// <summary>
/// Initializes everything.
/// </summary>
    private void initialize(Container container, List<Item> items) {
        itemsToPack = new ArrayList<>();
        itemsPackedInOrder = new ArrayList<>();

        // The original code uses 1-based indexing everywhere. This fake entry is added to the beginning
        // of the list to make that possible.
        itemsToPack.add(new Item(0, 0, 0, 0, 0));

        layers = new ArrayList<>();
        itemsToPackCount = 0;

        items.forEach(item ->
        {
            for (int i = 1; i <= item.getQuantity(); i++) {
                Item newItem = new Item(item.getId(), item.getDim1(), item.getDim2(), item.getDim3(), item.getQuantity());
                itemsToPack.add(newItem);
            }

            itemsToPackCount += item.getQuantity();
        });

        itemsToPack.add(new Item(0, 0, 0, 0, 0));

        totalContainerVolume = container.getLength() * container.getHeight() * container.getWidth();
        totalItemVolume = 0.0D;

        for (x = 1; x <= itemsToPackCount; x++) {
            totalItemVolume = totalItemVolume + itemsToPack.get(x).getVolume();
        }

        scrapFirst = new ScrapPad();

        scrapFirst.pre = null;
        scrapFirst.post = null;
        packingBest = false;
        hundredPercentPacked = false;
        quit = false;
    }

    /// <summary>
/// Lists all possible layer heights by giving a weight value to each of them.
/// </summary>
    private void listCanditLayers() {
        boolean same;
        double exdim = 0;
        double dimdif;
        double dimen2 = 0;
        double dimen3 = 0;
        int y;
        int z;
        int k;
        double layereval;

        layerListLen = 0;

        for (x = 1; x <= itemsToPackCount; x++) {
            for (y = 1; y <= 3; y++) {
                switch (y) {
                    case 1:
                        exdim = itemsToPack.get(x).getDim1();
                        dimen2 = itemsToPack.get(x).getDim2();
                        dimen3 = itemsToPack.get(x).getDim3();
                        break;

                    case 2:
                        exdim = itemsToPack.get(x).getDim2();
                        dimen2 = itemsToPack.get(x).getDim1();
                        dimen3 = itemsToPack.get(x).getDim3();
                        break;

                    case 3:
                        exdim = itemsToPack.get(x).getDim3();
                        dimen2 = itemsToPack.get(x).getDim1();
                        dimen3 = itemsToPack.get(x).getDim2();
                        break;
                }

                if ((exdim > py) || (((dimen2 > px) || (dimen3 > pz)) && ((dimen3 > px) || (dimen2 > pz)))) continue;

                same = false;

                for (k = 1; k <= layerListLen; k++) {
                    if (exdim == layers.get(k).layerDim) {
                        same = true;
                        continue;
                    }
                }

                if (same) continue;

                layereval = 0;

                for (z = 1; z <= itemsToPackCount; z++) {
                    if (!(x == z)) {
                        dimdif = Math.abs(exdim - itemsToPack.get(z).getDim1());

                        if (Math.abs(exdim - itemsToPack.get(z).getDim2()) < dimdif) {
                            dimdif = Math.abs(exdim - itemsToPack.get(z).getDim2());
                        }
                        if (Math.abs(exdim - itemsToPack.get(z).getDim3()) < dimdif) {
                            dimdif = Math.abs(exdim - itemsToPack.get(z).getDim3());
                        }
                        layereval = layereval + dimdif;
                    }
                }

                layerListLen++;

                layers.add(new Layer());
                layers.get(layerListLen).layerEval = layereval;
                layers.get(layerListLen).layerDim = exdim;
            }
        }
    }

    /// <summary>
/// Transforms the found coordinate system to the one entered by the user and writes them
/// to the report file.
/// </summary>
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

    /// <summary>
/// Packs the boxes found and arranges all variables and records properly.
/// </summary>
    private void packLayer() {
        double lenx;
        double lenz;
        double lpz;

        if (layerThickness == 0) {
            packing = false;
            return;
        }

        scrapFirst.cumX = px;
        scrapFirst.cumZ = 0;

        for (; !quit; ) {
            findSmallestZ();

            if ((smallestZ.pre == null) && (smallestZ.post == null)) {
                //*** SITUATION-1: NO BOXES ON THE RIGHT AND LEFT SIDES ***

                lenx = smallestZ.cumX;
                lpz = remainpz - smallestZ.cumZ;
                findBox(lenx, layerThickness, remainpy, lpz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordX(0);
                itemsToPack.get(cBoxI).setCoordY(packedy);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.cumZ);
                if (cboxx == smallestZ.cumX) {
                    smallestZ.cumZ = smallestZ.cumZ + cboxz;
                } else {
                    smallestZ.post = new ScrapPad();

                    smallestZ.post.post = null;
                    smallestZ.post.pre = smallestZ;
                    smallestZ.post.cumX = smallestZ.cumX;
                    smallestZ.post.cumZ = smallestZ.cumZ;
                    smallestZ.cumX = cboxx;
                    smallestZ.cumZ = smallestZ.cumZ + cboxz;
                }
            } else if (smallestZ.pre == null) {
                //*** SITUATION-2: NO BOXES ON THE LEFT SIDE ***

                lenx = smallestZ.cumX;
                lenz = smallestZ.post.cumZ - smallestZ.cumZ;
                lpz = remainpz - smallestZ.cumZ;
                findBox(lenx, layerThickness, remainpy, lenz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedy);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.cumZ);
                if (cboxx == smallestZ.cumX) {
                    itemsToPack.get(cBoxI).setCoordX(0);

                    if (smallestZ.cumZ + cboxz == smallestZ.post.cumZ) {
                        smallestZ.cumZ = smallestZ.post.cumZ;
                        smallestZ.cumX = smallestZ.post.cumX;
                        smallestZ.post = smallestZ.post.post;

                        if (smallestZ.post != null) {
                            smallestZ.post.pre = smallestZ;
                        }
                    } else {
                        smallestZ.cumZ = smallestZ.cumZ + cboxz;
                    }
                } else {
                    itemsToPack.get(cBoxI).setCoordX(smallestZ.cumX - cboxx);

                    if (smallestZ.cumZ + cboxz == smallestZ.post.cumZ) {
                        smallestZ.cumX = smallestZ.cumX - cboxx;
                    } else {
                        smallestZ.post.pre = new ScrapPad();

                        smallestZ.post.pre.post = smallestZ.post;
                        smallestZ.post.pre.pre = smallestZ;
                        smallestZ.post = smallestZ.post.pre;
                        smallestZ.post.cumX = smallestZ.cumX;
                        smallestZ.cumX = smallestZ.cumX - cboxx;
                        smallestZ.post.cumZ = smallestZ.cumZ + cboxz;
                    }
                }
            } else if (smallestZ.post == null) {
                //*** SITUATION-3: NO BOXES ON THE RIGHT SIDE ***

                lenx = smallestZ.cumX - smallestZ.pre.cumX;
                lenz = smallestZ.pre.cumZ - smallestZ.cumZ;
                lpz = remainpz - smallestZ.cumZ;
                findBox(lenx, layerThickness, remainpy, lenz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedy);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.cumZ);
                itemsToPack.get(cBoxI).setCoordX(smallestZ.pre.cumX);

                if (cboxx == smallestZ.cumX - smallestZ.pre.cumX) {
                    if (smallestZ.cumZ + cboxz == smallestZ.pre.cumZ) {
                        smallestZ.pre.cumX = smallestZ.cumX;
                        smallestZ.pre.post = null;
                    } else {
                        smallestZ.cumZ = smallestZ.cumZ + cboxz;
                    }
                } else {
                    if (smallestZ.cumZ + cboxz == smallestZ.pre.cumZ) {
                        smallestZ.pre.cumX = smallestZ.pre.cumX + cboxx;
                    } else {
                        smallestZ.pre.post = new ScrapPad();

                        smallestZ.pre.post.pre = smallestZ.pre;
                        smallestZ.pre.post.post = smallestZ;
                        smallestZ.pre = smallestZ.pre.post;
                        smallestZ.pre.cumX = smallestZ.pre.pre.cumX + cboxx;
                        smallestZ.pre.cumZ = smallestZ.cumZ + cboxz;
                    }
                }
            } else if (smallestZ.pre.cumZ == smallestZ.post.cumZ) {
                //*** SITUATION-4: THERE ARE BOXES ON BOTH OF THE SIDES ***

                //*** SUBSITUATION-4A: SIDES ARE EQUAL TO EACH OTHER ***

                lenx = smallestZ.cumX - smallestZ.pre.cumX;
                lenz = smallestZ.pre.cumZ - smallestZ.cumZ;
                lpz = remainpz - smallestZ.cumZ;

                findBox(lenx, layerThickness, remainpy, lenz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedy);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.cumZ);

                if (cboxx == smallestZ.cumX - smallestZ.pre.cumX) {
                    itemsToPack.get(cBoxI).setCoordX(smallestZ.pre.cumX);

                    if (smallestZ.cumZ + cboxz == smallestZ.post.cumZ) {
                        smallestZ.pre.cumX = smallestZ.post.cumX;

                        if (smallestZ.post.post != null) {
                            smallestZ.pre.post = smallestZ.post.post;
                            smallestZ.post.post.pre = smallestZ.pre;
                        } else {
                            smallestZ.pre.post = null;
                        }
                    } else {
                        smallestZ.cumZ = smallestZ.cumZ + cboxz;
                    }
                } else if (smallestZ.pre.cumX < px - smallestZ.cumX) {
                    if (smallestZ.cumZ + cboxz == smallestZ.pre.cumZ) {
                        smallestZ.cumX = smallestZ.cumX - cboxx;
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.cumX - cboxx);
                    } else {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.pre.cumX);
                        smallestZ.pre.post = new ScrapPad();

                        smallestZ.pre.post.pre = smallestZ.pre;
                        smallestZ.pre.post.post = smallestZ;
                        smallestZ.pre = smallestZ.pre.post;
                        smallestZ.pre.cumX = smallestZ.pre.pre.cumX + cboxx;
                        smallestZ.pre.cumZ = smallestZ.cumZ + cboxz;
                    }
                } else {
                    if (smallestZ.cumZ + cboxz == smallestZ.pre.cumZ) {
                        smallestZ.pre.cumX = smallestZ.pre.cumX + cboxx;
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.pre.cumX);
                    } else {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.cumX - cboxx);
                        smallestZ.post.pre = new ScrapPad();

                        smallestZ.post.pre.post = smallestZ.post;
                        smallestZ.post.pre.pre = smallestZ;
                        smallestZ.post = smallestZ.post.pre;
                        smallestZ.post.cumX = smallestZ.cumX;
                        smallestZ.post.cumZ = smallestZ.cumZ + cboxz;
                        smallestZ.cumX = smallestZ.cumX - cboxx;
                    }
                }
            } else {
                //*** SUBSITUATION-4B: SIDES ARE NOT EQUAL TO EACH OTHER ***

                lenx = smallestZ.cumX - smallestZ.pre.cumX;
                lenz = smallestZ.pre.cumZ - smallestZ.cumZ;
                lpz = remainpz - smallestZ.cumZ;
                findBox(lenx, layerThickness, remainpy, lenz, lpz);
                checkFound();

                if (layerDone) break;
                if (evened) continue;

                itemsToPack.get(cBoxI).setCoordY(packedy);
                itemsToPack.get(cBoxI).setCoordZ(smallestZ.cumZ);
                itemsToPack.get(cBoxI).setCoordX(smallestZ.pre.cumX);

                if (cboxx == (smallestZ.cumX - smallestZ.pre.cumX)) {
                    if ((smallestZ.cumZ + cboxz) == smallestZ.pre.cumZ) {
                        smallestZ.pre.cumX = smallestZ.cumX;
                        smallestZ.pre.post = smallestZ.post;
                        smallestZ.post.pre = smallestZ.pre;
                    } else {
                        smallestZ.cumZ = smallestZ.cumZ + cboxz;
                    }
                } else {
                    if ((smallestZ.cumZ + cboxz) == smallestZ.pre.cumZ) {
                        smallestZ.pre.cumX = smallestZ.pre.cumX + cboxx;
                    } else if (smallestZ.cumZ + cboxz == smallestZ.post.cumZ) {
                        itemsToPack.get(cBoxI).setCoordX(smallestZ.cumX - cboxx);
                        smallestZ.cumX = smallestZ.cumX - cboxx;
                    } else {
                        smallestZ.pre.post = new ScrapPad();

                        smallestZ.pre.post.pre = smallestZ.pre;
                        smallestZ.pre.post.post = smallestZ;
                        smallestZ.pre = smallestZ.pre.post;
                        smallestZ.pre.cumX = smallestZ.pre.pre.cumX + cboxx;
                        smallestZ.pre.cumZ = smallestZ.cumZ + cboxz;
                    }
                }
            }

            volumeCheck();
        }
    }

    private void changeVariant(long x, long y, long z) {
        px = x;
        py = y;
        pz = z;
    }

    /// <summary>
/// Using the parameters found, packs the best solution found and
/// reports to the console.
/// </summary>
    private void report(Container container) {
        quit = false;

        switch (bestVariant) {
            case 1:
                changeVariant(container.getLength(), container.getHeight(), container.getWidth());
                break;
            case 2:
                changeVariant(container.getWidth(), container.getHeight(), container.getLength());
                break;
            case 3:
                changeVariant(container.getWidth(), container.getLength(), container.getHeight());
                break;
            case 4:
                changeVariant(container.getHeight(), container.getLength(), container.getWidth());
                break;
            case 5:
                changeVariant(container.getLength(), container.getWidth(), container.getHeight());
                break;
            case 6:
                changeVariant(container.getHeight(), container.getWidth(), container.getLength());
                break;
        }

        packingBest = true;

        System.out.println("BEST SOLUTION FOUND AT ITERATION       :" + bestIteration + " OF VARIANT " + bestVariant);
        System.out.println("TOTAL ITEMS TO PACK                    :" + itemsToPackCount);
        System.out.println("TOTAL VOLUME OF ALL ITEMS              :" + totalItemVolume);
        System.out.println("WHILE CONTAINER ORIENTATION X - Y - Z  :" + px + py + pz);

        layers.clear();
        layers.add(new Layer(0, -1));
        listCanditLayers();
        layers = layers.stream().sorted(Comparator.comparingDouble(l -> l.layerEval)).collect(Collectors.toList());
        packedVolume = 0;
        packedy = 0;
        packing = true;
        layerThickness = layers.get(bestIteration).layerDim;
        remainpy = py;
        remainpz = pz;

        for (x = 1; x <= itemsToPackCount; x++) {
            itemsToPack.get(x).setPacked(false);
        }

        do {
            layerinlayer = 0;
            layerDone = false;
            packLayer();
            packedy = packedy + layerThickness;
            remainpy = py - packedy;

            if (layerinlayer > 0.0001D) {
                prepackedy = packedy;
                preremainpy = remainpy;
                remainpy = layerThickness - prelayer;
                packedy = packedy - layerThickness + prelayer;
                remainpz = lilz;
                layerThickness = layerinlayer;
                layerDone = false;
                packLayer();
                packedy = prepackedy;
                remainpy = preremainpy;
                remainpz = pz;
            }

            if (!quit) {
                findLayer(remainpy);
            }
        } while (packing && !quit);
    }

    /**
     * After packing of each item, the 100% packing condition is checked.
     */
    private void volumeCheck() {
        itemsToPack.get(cBoxI).setPacked(true);
        itemsToPack.get(cBoxI).setPackDimX(cboxx);
        itemsToPack.get(cBoxI).setPackDimY(cboxy);
        itemsToPack.get(cBoxI).setPackDimZ(cboxz);
        packedVolume = packedVolume + itemsToPack.get(cBoxI).getVolume();

        if (packingBest) {
            outputBoxList();
        } else if (packedVolume == totalContainerVolume || packedVolume == totalItemVolume) {
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
        /// <summary>
        /// Gets or sets the layer dimension value, representing a layer thickness.
        /// </summary>
        /// <value>
        /// The layer dimension value.
        /// </value>
        public double layerDim;

        /// <summary>
        /// Gets or sets the layer eval value, representing an evaluation weight
        /// value for the corresponding layerDim value.
        /// </summary>
        /// <value>
        /// The layer eval value.
        /// </value>
        public double layerEval;
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
        /// <summary>
        /// Gets or sets the x coordinate of the gap's right corner.
        /// </summary>
        /// <value>
        /// The x coordinate of the gap's right corner.
        /// </value>
        public double cumX;

        /// <summary>
        /// Gets or sets the z coordinate of the gap's right corner.
        /// </summary>
        /// <value>
        /// The z coordinate of the gap's right corner.
        /// </value>
        public double cumZ;

        /// <summary>
        /// Gets or sets the following entry.
        /// </summary>
        /// <value>
        /// The following entry.
        /// </value>
        public ScrapPad post;

        /// <summary>
        /// Gets or sets the previous entry.
        /// </summary>
        /// <value>
        /// The previous entry.
        /// </value>
        public ScrapPad pre;

    }
}