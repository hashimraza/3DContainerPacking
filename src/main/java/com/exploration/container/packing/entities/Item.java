package com.exploration.container.packing.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private long weight;

    public double getVolume() {
        if (volume == 0) {
            this.setVolume(dim1 * dim2 * dim3);
        }

        return volume;
    }

}
