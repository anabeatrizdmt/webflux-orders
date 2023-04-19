package com.example.ordersapi.repository;

import com.example.ordersapi.model.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderRepository {

    private static final List<Order> orders_db = new CopyOnWriteArrayList<>();

    public void save(Order order) {
        orders_db.add(order);
    }

    public Optional<Order> findById(String id) {
        return orders_db.stream()
                .filter(entity -> entity.id().equals(id))
                .findFirst();
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders_db);
    }
}
