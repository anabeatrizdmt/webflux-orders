package com.example.ordersapi.pubsub;

import com.example.ordersapi.model.Order;

public record PubSubMessage (String id, Order order) {
}
