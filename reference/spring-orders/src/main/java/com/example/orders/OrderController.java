package com.example.orders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderService service;
    private final OutcomeHttpBoundary boundary;

    OrderController(OrderService service, OutcomeHttpBoundary boundary) {
        this.service = service;
        this.boundary = boundary;
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody CreateOrderRequest request) {
        return boundary.toResponse(service.create(request));
    }

    @GetMapping("/{orderId}")
    ResponseEntity<?> find(@PathVariable String orderId) {
        return boundary.toResponse(service.find(orderId));
    }

    @PostMapping("/{orderId}/process")
    ResponseEntity<?> process(@PathVariable String orderId) {
        return boundary.toResponse(service.process(orderId));
    }
}
