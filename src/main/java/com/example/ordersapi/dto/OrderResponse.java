package com.example.ordersapi.dto;

import com.example.ordersapi.model.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse (
        String id,
        List<Order.OrderProduct> productList,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Order.Status status,
        BigDecimal totalAmount,
        String userId
) {}
