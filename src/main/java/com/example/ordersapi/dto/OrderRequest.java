package com.example.ordersapi.dto;

import java.util.List;

public record OrderRequest(List<ProductDTO> products) {}
