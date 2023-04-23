package com.example.ordersapi.client;

import com.example.ordersapi.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.List;


@Component
public class CatalogClient {

    private static final String STOCK_URI = "/catalog/stock";
    private final WebClient client;
    private final ObjectMapper mapper;

    public CatalogClient(WebClient.Builder clientBuilder, ObjectMapper mapper) {
        this.client = clientBuilder.baseUrl("http://localhost:8081").build();
        this.mapper = mapper;
    }

    public Flux<Product> getProductStock(List<String> ids) {
        return client.post()
                .uri(STOCK_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ids))
                .retrieve()
                .bodyToFlux(Product.class);
    }
}
