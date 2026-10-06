package com.example.orders;

final class ArchivedOrderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String orderId;

    ArchivedOrderNotFoundException(String orderId) {
        this.orderId = orderId;
    }

    String orderId() {
        return orderId;
    }
}
