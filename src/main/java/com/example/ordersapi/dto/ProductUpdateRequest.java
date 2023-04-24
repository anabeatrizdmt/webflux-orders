package com.example.ordersapi.dto;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String productId;
    private Long purchasedQuantity;

    public ProductUpdateRequest(String productId, Long purchasedQuantity) {
        this.productId = productId;
        this.purchasedQuantity = purchasedQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public Long getPurchasedQuantity() {
        return purchasedQuantity;
    }
}
