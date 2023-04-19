package com.example.ordersapi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//@Data
//@Builder
//@With
//@Document(value = "orders")
public record Order (
        String id,
        List<Product> productsList,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Status status,
        BigDecimal totalAmount
    //    public String userId;
) {

    public static record Product (
//        String id,
            String productId,
            Long quantity
//            BigDecimal amount
    ) {}
    public enum Status {
        PLACED, CONFIRMED, ERROR_IN_ORDER, SENT_FOR_DELIVERY
    }
}

