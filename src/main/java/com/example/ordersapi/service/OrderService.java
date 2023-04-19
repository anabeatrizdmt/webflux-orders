package com.example.ordersapi.service;

import com.example.ordersapi.dto.OrderResponse;
import com.example.ordersapi.dto.ProductDTO;
import com.example.ordersapi.model.Order;
import com.example.ordersapi.repository.OrderRepository;
import com.example.ordersapi.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    public Mono<OrderResponse> save(OrderRequest orderDTO) {

        String uid = UUID.randomUUID().toString();

        var productsEntity = orderDTO.products().stream()
                .map(dto -> new Order.Product(dto.productId(), dto.quantity()))
                .toList();

        var orderEntity = new Order(
                uid,
                productsEntity,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Order.Status.PLACED,
                BigDecimal.ZERO
                );

        repository.save(orderEntity);

        return Mono.defer(() -> Mono.just(new OrderResponse(uid, orderDTO.products(), Order.Status.PLACED)));
    }

    public Mono<OrderResponse> findById(String id) {
        return Mono.defer(() -> Mono.justOrEmpty(repository.findById(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(entity -> {
                    var productsDTO = entity.productsList().stream()
                            .map(it -> new ProductDTO(it.productId(), it.quantity()))
                            .toList();
                    return new OrderResponse(entity.id(), productsDTO, entity.status());
                });
    }
}
