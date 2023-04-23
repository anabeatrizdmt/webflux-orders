package com.example.ordersapi.client;

import com.example.ordersapi.model.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserClient {

    private static final String USER_URI = "/users/status";
    private final WebClient client;
    private final ObjectMapper mapper;

    public UserClient(WebClient.Builder clientBuilder, ObjectMapper mapper) {
        this.client = clientBuilder.baseUrl("http://localhost:8083").build();
        this.mapper = mapper;
    }

    public Mono<UserStatus> getUserStatus(String id) {
        return client.get()
                .uri(USER_URI + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserStatus.class);
    }
}
