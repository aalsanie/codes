package com.example.orders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
class OrderController {

    @GetMapping("/{orderId}")
    String find(@PathVariable String orderId) {
        throw new OrderNotFoundException(orderId);
    }

    @GetMapping("/archive/{orderId}")
    String findArchived(@PathVariable String orderId) {
        throw new ArchivedOrderNotFoundException(orderId);
    }
}

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
