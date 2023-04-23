package com.example.ordersapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    @JsonProperty("productList")
    private List<ProductDTO> productList;

    @JsonProperty("userId")
    private String userId;
}
