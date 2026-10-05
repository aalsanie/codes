package com.example.orders;

final class OrderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String orderId;

    OrderNotFoundException(String orderId) {
        this.orderId = orderId;
    }

    String orderId() {
        return orderId;
    }
}
