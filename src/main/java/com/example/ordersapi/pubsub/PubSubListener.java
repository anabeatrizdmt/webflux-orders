package com.example.ordersapi.pubsub;

import com.example.ordersapi.client.CatalogClient;
import com.example.ordersapi.client.CatalogStockClient;
import com.example.ordersapi.client.UserClient;
import com.example.ordersapi.dto.ProductUpdateRequest;
import com.example.ordersapi.model.Order;
import com.example.ordersapi.model.UserStatus;
import com.example.ordersapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubListener implements InitializingBean {
    private final Sinks.Many<PubSubMessage> sink;
    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final CatalogStockClient catalogStockClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        sink.asFlux()
                .delayElements(Duration.ofSeconds(10))
                .subscribe(
                        next -> {
                            log.info("Starting listener onNext - {}", next);
                            final var id = next.id();
                            orderRepository.findById(id)
                                    .flatMap(this::updateOrderStatus)
                                    .flatMap(this::updateCatalogStock)
                                    .subscribe(
                                            order -> log.info("Order updated - {}", order),
                                            err -> log.error("Error: {}", err.getMessage()),
                                            () -> log.info("Completed")
                                    );
                        }
                );
    }

    private Mono<Order> updateOrderStatus(Order order) {
        log.info("Checking user status - {}", order);
        String userId = order.getUserId();
        return userClient.getUserStatus(userId)
                .flatMap(userStatus -> {
                    if (userStatus != UserStatus.ACTIVE) {
                        log.info("Changing order status - {}", order);
                        order.setStatus(Order.Status.ERROR_IN_ORDER);
                    } else {
                        log.info("Changing order status - {}", order);
                        order.setStatus(Order.Status.CONFIRMED);
                    }
                    order.setUpdatedAt(LocalDateTime.now());
                    return orderRepository.save(order);
                });
    }

    private Mono<Order> updateCatalogStock(Order order) {
        log.info("Updating catalog stock - {}", order);
        if (order.getStatus().equals(Order.Status.CONFIRMED)) {
            List<ProductUpdateRequest> productUpdates = order.getProductsList().stream()
                    .map(product -> new ProductUpdateRequest(product.getProductId(), product.getQuantity()))
                    .collect(Collectors.toList());
            order.setStatus(Order.Status.SENT_FOR_DELIVERY);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order)
                    .flatMap(savedOrder -> {
                        log.info("Updating catalog stock - {}", savedOrder);
                        return catalogStockClient.updateStock(productUpdates)
                                .thenReturn(savedOrder);
                    });
        } else {
            return Mono.just(order);
        }
    }
}
