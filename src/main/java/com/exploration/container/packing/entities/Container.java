package com.exploration.container.packing.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Container {
    public int id;
    public long length;
    public long width;
    public long height;
    public long volume;
    public Container(int id, long length, long width, long height) {
        this.id = id;
        this.length = length;
        this.width = width;
        this.height = height;
        this.volume = length * width * height;
    }
}
