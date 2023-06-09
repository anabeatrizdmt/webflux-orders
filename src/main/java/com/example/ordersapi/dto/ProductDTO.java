package com.example.ordersapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductDTO (
        @JsonProperty("product")
        String productId,
        @JsonProperty("quantity")
        Long quantity
) {
}
