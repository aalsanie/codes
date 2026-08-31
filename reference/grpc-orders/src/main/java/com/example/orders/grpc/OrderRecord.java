package com.example.orders.grpc;

record OrderRecord(String orderId, String customerId, String product, String status) {
}
