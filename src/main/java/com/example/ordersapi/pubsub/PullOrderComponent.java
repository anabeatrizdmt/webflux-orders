package com.example.ordersapi.pubsub;


import com.example.ordersapi.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PullOrderComponent {

    private final Sinks.Many<PubSubMessage> sink;

    public Mono<Order> pullNewOrder(final Order order) {
        return Mono.fromCallable(() -> {
                    log.info("Starting order pull - {}", order);
                    String id = order.getId();
                    return new PubSubMessage(id, order);
                })
                .subscribeOn(Schedulers.parallel())
                .doOnNext(pubSubMessage -> this.sink.tryEmitNext(pubSubMessage))
                .doOnNext(event -> log.info("Order event created - {}", event))
                .thenReturn(order);
    }
}
