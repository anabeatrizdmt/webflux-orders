package com.example.ordersapi.controller.handler;

import com.example.ordersapi.dto.OrderRequest;
import com.example.ordersapi.dto.OrderResponse;
import com.example.ordersapi.model.Product;
import com.example.ordersapi.pubsub.PullOrderComponent;
import com.example.ordersapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderService orderService;
    private final PullOrderComponent pullOrderComponent;


    public Mono<ServerResponse> save(ServerRequest request) {

        return request.bodyToMono(OrderRequest.class)
                .flatMap(orderService::save)
                .flatMap(order -> pullOrderComponent.pullNewOrder(order))
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(response)));

    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<OrderResponse> orderResponses = orderService
                .getAll()
                .map(order -> OrderResponse.builder()
                        .id(order.getId())
                        .productList(order.getProductsList())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .status(order.getStatus())
                        .totalAmount(order.getTotalAmount())
                        .userId(order.getUserId())
                        .build());

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(orderResponses, OrderResponse.class));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {

        String id = request.pathVariable("id");
        Mono<OrderResponse> responseMono = orderService.findById(id)
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getProductsList(),
                        order.getCreatedAt(),
                        order.getUpdatedAt(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getUserId()
                ));

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                        .fromPublisher(responseMono, OrderResponse.class));

    }

    public Mono<ServerResponse> getProducts(ServerRequest request) {
        List<String> ids = request.queryParam("ids")
                .map(param -> Arrays.asList(param.split(",")))
                .orElse(Collections.emptyList());

        Flux<Product> products = orderService.getProducts(ids);

        return ServerResponse.ok().body(products, Product.class);
    }
}
