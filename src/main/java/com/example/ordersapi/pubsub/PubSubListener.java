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
                                    .flatMap(order -> {
                                        log.info("Checking user status - {}", order);
                                        String userId = order.getUserId();
                                        return userClient.getUserStatus(userId)
                                                .flatMap(userStatus -> {
                                                    if (userStatus != UserStatus.ACTIVE) {
                                                        log.info("Changing order status - {}", order);
                                                        order.setStatus(Order.Status.ERROR_IN_ORDER);
                                                        order.setUpdatedAt(LocalDateTime.now());
                                                        return orderRepository.save(order);
                                                    } else {
                                                        log.info("Changing order status - {}", order);
                                                        order.setStatus(Order.Status.CONFIRMED);
                                                        order.setUpdatedAt(LocalDateTime.now());
                                                        return orderRepository.save(order);                                                    }
                                                }).flatMap(updatedOrder -> {
                                                    log.info("2-Changing order status - {}", order);
                                                    log.info("3-Changing order status - {}", updatedOrder);
                                                    if (updatedOrder.getStatus().equals(Order.Status.CONFIRMED)) {
                                                        log.info("Test - Changing order status - {}", order);
                                                        List<ProductUpdateRequest> productUpdates = updatedOrder.getProductsList().stream()
                                                                .map(product -> new ProductUpdateRequest(product.getProductId(), product.getQuantity()))
                                                                .collect(Collectors.toList());
                                                        order.setStatus(Order.Status.SENT_FOR_DELIVERY);
                                                        order.setUpdatedAt(LocalDateTime.now());

                                                        return orderRepository.save(order)
                                                                .flatMap(savedOrder -> {
                                                                    log.info("4-Changing order status - {}", savedOrder);
                                                                    return catalogStockClient.updateStock(productUpdates);
                                                                });
                                                    } else {
                                                        return Mono.just(updatedOrder);
                                                    }
                                                })
                                                .onErrorResume(err -> {
                                                    log.error("Error: {}", err.getMessage());
                                                    return Mono.empty();
                                                });
                                    })
                                    .subscribe(
                                            order -> log.info("Order updated - {}", order),
                                            err -> log.error("Error: {}", err.getMessage()),
                                            () -> log.info("Completed")
                                    );
                        }
                );
    }

}
