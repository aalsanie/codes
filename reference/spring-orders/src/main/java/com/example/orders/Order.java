package com.example.orders;

public record Order(String orderId, String customerId, String product, String status) {
    Order withStatus(String nextStatus) {
        return new Order(orderId, customerId, product, nextStatus);
    }
}
