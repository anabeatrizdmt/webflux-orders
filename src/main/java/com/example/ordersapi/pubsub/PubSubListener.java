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
                                                .filter(userStatus -> userStatus == UserStatus.ACTIVE)
                                                .switchIfEmpty(Mono.error(new IllegalStateException("User status is BLOCKED")))
                                                .map(userStatus -> order);
                                    })
                                    .flatMap(order -> {
                                        log.info("Changing order status - {}", order);
                                        var confirmedStatus = Order.Status.CONFIRMED;
                                        return orderRepository.save(next.order()
                                                .withStatus(confirmedStatus)
                                                .withUpdatedAt(LocalDateTime.now()));
                                    })
                                    .subscribe(
                                            order -> log.info("Order confirmed - {}", order),
                                            err -> log.error("Error: {}", err.getMessage()),
                                            () -> log.info("Completed")
                                    );
                        }
                );
    }

}
