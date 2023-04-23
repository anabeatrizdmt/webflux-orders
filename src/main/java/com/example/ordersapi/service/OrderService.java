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

        Flux<Product> stockFlux = catalogClient.getProductStock(productIds);

        return stockFlux.collectMap(Product::getId, Product::getAvailableQuantity)
                .flatMap(stockMap -> {
                    for (Order.OrderProduct product : productsEntity) {
                        var stock = stockMap.get(product.getProductId());
                        if (stock == null || stock < product.quantity()) {
                            return Mono.error(new RuntimeException("Insufficient stock for product with ID " + product.getProductId()));
                        }
                    }

                    BigDecimal totalAmount = calculateTotalAmount(productsEntity);

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
        Flux<Product> products = catalogClient.getProductStock(ids);
        return products;
    }

    private BigDecimal calculateTotalAmount(List<Order.OrderProduct> productsEntity) {
        return BigDecimal.ZERO;
    }
}
