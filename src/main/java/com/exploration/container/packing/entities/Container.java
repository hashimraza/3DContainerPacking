package com.exploration.container.packing.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Container {
    private int id;
    private long length;
    private long width;
    private long height;
    private long weight;
    private long maxAllowedWeight;
}
