package com.example.ordersapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@With
@Document(value = "orders")
public class Order {

    @Id
    private String id;
    private List<Product> productsList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Status status;
    private BigDecimal totalAmount;
    //    public String userId;

    public static record Product (
//        String id,
            String productId,
            Long quantity
//            BigDecimal amount
    ) {}
    public enum Status {
        PLACED, CONFIRMED, ERROR_IN_ORDER, SENT_FOR_DELIVERY
    }
}

