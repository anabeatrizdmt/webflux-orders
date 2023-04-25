package com.example.ordersapi.service;

import com.example.ordersapi.client.CatalogClient;
import com.example.ordersapi.model.Order;
import com.example.ordersapi.model.Product;
import com.example.ordersapi.repository.OrderRepository;
import com.example.ordersapi.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final CatalogClient catalogClient;


    public Mono<Order> save(OrderRequest request) {
        var productsEntity = request.getProductList().stream()
                .map(dto -> new Order.OrderProduct(dto.productId(), dto.quantity()))
                .toList();

        var productIds = productsEntity.stream().map(Order.OrderProduct::productId).collect(Collectors.toList());

        Flux<Product> productFlux = catalogClient.getProductInfo(productIds);

        Mono<BigDecimal> totalAmountMono = calculateTotalAmount(Flux.fromIterable(productsEntity));

        return productFlux.collectMap(Product::getId, Product::getAvailableQuantity)
                .zipWith(totalAmountMono)
                .flatMap(tuple -> {
                    var stockMap = tuple.getT1();
                    var totalAmount = tuple.getT2();

                    for (Order.OrderProduct product : productsEntity) {
                        var stock = stockMap.get(product.getProductId());
                        if (stock == null || stock < product.quantity()) {
                            return Mono.error(new RuntimeException("Insufficient stock for product with ID " + product.getProductId()));
                        }
                    }

                    Order order = Order.builder()
                            .id(UUID.randomUUID().toString())
                            .productsList(productsEntity)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .status(Order.Status.PLACED)
                            .totalAmount(totalAmount)
                            .userId(request.getUserId())
                            .build();

                    return repository.save(order);
                });
    }

    public Flux<Order> getAll() {
        return repository.findAll();
    }

    public Mono<Order> findById(String id) {
        return repository.findById(id);
    }

    public Flux<Product> getProducts(List<String> ids) {
        Flux<Product> products = catalogClient.getProductInfo(ids);
        return products;
    }

    private Mono<BigDecimal> calculatePartialAmount(List<String> productIds, Long quantity) {
        return catalogClient.getProductInfo(productIds)
                .collectList()
                .map(products -> {
                    BigDecimal price = BigDecimal.ZERO;
                    for (Product product : products) {
                        price = price.add(product.getPrice());
                    }
                    return price.multiply(BigDecimal.valueOf(quantity));
                });
    }

    private Mono<BigDecimal> calculateTotalAmount(Flux<Order.OrderProduct> products) {
        return products.flatMap(product -> calculatePartialAmount(Collections.singletonList(product.getProductId()), product.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
