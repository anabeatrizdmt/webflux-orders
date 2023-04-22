package com.example.ordersapi.repository;

import com.example.ordersapi.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository  extends ReactiveMongoRepository<Order, String> {
}
