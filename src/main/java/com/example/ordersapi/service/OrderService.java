package com.example.ordersapi.service;

import com.example.ordersapi.model.Order;
import com.example.ordersapi.repository.OrderRepository;
import com.example.ordersapi.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    public Mono<Order> save(OrderRequest request) {

        var productsEntity = request.getProductList().stream()
                .map(dto -> new Order.Product(dto.productId(), dto.quantity()))
                .toList();

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .productsList(productsEntity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Order.Status.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .userId(request.getUserId())
                .build();

        return repository.save(order);
    }

    public Flux<Order> getAll() {
        return repository.findAll();
    }

    public Mono<Order> findById(String id) {
        return repository.findById(id);
    }
}
