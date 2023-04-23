package com.example.ordersapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;

@Data
@Builder
@With
public class Product {
    private String id;
    private String name;
    private BigDecimal price;
    private Long availableQuantity;
}
