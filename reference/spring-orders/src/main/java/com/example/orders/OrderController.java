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
