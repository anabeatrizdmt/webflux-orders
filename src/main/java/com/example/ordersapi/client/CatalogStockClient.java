package com.example.ordersapi.client;

import com.example.ordersapi.dto.ProductUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CatalogStockClient {

    private static final String STOCK_URI = "/catalog/update-stock";
    private final WebClient client;
    private final ObjectMapper mapper;

    public CatalogStockClient(WebClient.Builder clientBuilder, ObjectMapper mapper) {
        this.client = clientBuilder.baseUrl("http://localhost:8081").build();
        this.mapper = mapper;
    }

    public Mono<Void> updateStock(List<ProductUpdateRequest> productUpdates) {
        return client
                .post()
                .uri(STOCK_URI)
                .body(BodyInserters.fromValue(productUpdates))
                .retrieve()
                .bodyToMono(Void.class);
    }

}
