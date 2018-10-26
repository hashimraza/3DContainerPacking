package com.exploration.container.packing.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Container {
    private int id;
    private long length;
    private long width;
    private long height;
    private long volume;

    public Container(int id, long length, long width, long height) {
        this.setId(id);
        this.setLength(length);
        this.setWidth(width);
        this.setHeight(height);
        this.setVolume(length * width * height);
    }
}
