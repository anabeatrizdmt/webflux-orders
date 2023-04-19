package com.example.ordersapi.controller.router;

import com.example.ordersapi.controller.handler.OrderHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@RequiredArgsConstructor
public class OrderRouter {

    private final OrderHandler handler;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions
                .route(GET("/orders/{id}"), handler::findById)
                .andRoute(POST("/orders"), handler::save);
    }
}
