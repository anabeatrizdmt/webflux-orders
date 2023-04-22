package com.example.ordersapi.controller.handler;

import com.example.ordersapi.dto.OrderRequest;
import com.example.ordersapi.dto.OrderResponse;
import com.example.ordersapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderService orderService;

    public Mono<ServerResponse> save(ServerRequest request) {

        return request.bodyToMono(OrderRequest.class)
                .flatMap(orderService::save)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(response)));

    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<OrderResponse> orderResponses = orderService
                .getAll()
                .map(product -> OrderResponse.builder()
                        .id(product.getId())
                        .productList(product.getProductsList())
                        .createdAt(product.getCreatedAt())
                        .updatedAt(product.getUpdatedAt())
                        .status(product.getStatus())
                        .totalAmount(product.getTotalAmount())
                        .build());

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(orderResponses, OrderResponse.class));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {

        String id = request.pathVariable("id");
        Mono<OrderResponse> responseMono = orderService.findById(id)
                .map(product -> new OrderResponse(
                        product.getId(),
                        product.getProductsList(),
                        product.getCreatedAt(),
                        product.getUpdatedAt(),
                        product.getStatus(),
                        product.getTotalAmount()
                ));

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                        .fromPublisher(responseMono, OrderResponse.class));

    }
}
