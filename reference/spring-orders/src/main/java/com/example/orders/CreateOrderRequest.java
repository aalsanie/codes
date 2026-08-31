package com.example.orders;

public record CreateOrderRequest(String orderId, String customerId, String product) {
}
