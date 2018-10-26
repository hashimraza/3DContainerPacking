package com.exploration.container.packing.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Item {
    public int id;
    public boolean isPacked;
    public double dim1;
    public double dim2;
    public double dim3;
    public double coordX;
    public double coordY;
    public double coordZ;
    public int quantity;
    public double packDimX;
    public double packDimY;
    public double packDimZ;
    public double volume;

    public Item(int id, double dim1, double dim2, double dim3, int quantity) {
        this.id = id;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.dim3 = dim3;
        this.volume = dim1 * dim2 * dim3;
        this.quantity = quantity;
    }
}
