package com.example.ordersapi.pubsub;

import com.example.ordersapi.client.UserClient;
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
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubListener implements InitializingBean {
    private final Sinks.Many<PubSubMessage> sink;
    private final OrderRepository orderRepository;
    private final UserClient client;

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
                                        return client.getUserStatus(userId)
                                                .flatMap(userStatus -> {
                                                    if (userStatus != UserStatus.ACTIVE) {
                                                        order.setStatus(Order.Status.ERROR_IN_ORDER);
                                                        order.setUpdatedAt(LocalDateTime.now());
                                                        return orderRepository.save(order);
                                                    } else {
                                                        log.info("Changing order status - {}", order);
                                                        var confirmedStatus = Order.Status.CONFIRMED;
                                                        return orderRepository.save(order.withStatus(confirmedStatus).withUpdatedAt(LocalDateTime.now()));
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
