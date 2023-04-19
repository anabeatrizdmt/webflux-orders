package com.example.ordersapi.dto;

import com.example.ordersapi.model.Order;

import java.util.List;

public record OrderResponse (String id, List<ProductDTO> products, Order.Status status) {}
