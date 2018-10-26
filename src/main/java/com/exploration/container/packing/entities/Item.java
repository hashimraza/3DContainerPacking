package com.exploration.container.packing.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Item {
    private int id;
    private boolean isPacked;
    private double dim1;
    private double dim2;
    private double dim3;
    private double coordX;
    private double coordY;
    private double coordZ;
    private int quantity;
    private double packDimX;
    private double packDimY;
    private double packDimZ;
    private double volume;

    public Item(int id, double dim1, double dim2, double dim3, int quantity) {
        this.setId(id);
        this.setDim1(dim1);
        this.setDim2(dim2);
        this.setDim3(dim3);
        this.setVolume(dim1 * dim2 * dim3);
        this.setQuantity(quantity);
    }
}
