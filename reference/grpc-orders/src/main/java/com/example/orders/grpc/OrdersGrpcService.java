package com.example.orders.grpc;

import io.grpc.stub.StreamObserver;

final class OrdersGrpcService extends OrdersGrpc.OrdersImplBase {
    private final OrderService service;
    private final OutcomeGrpcBoundary boundary;

    OrdersGrpcService(OrderService service, OutcomeGrpcBoundary boundary) {
        this.service = service;
        this.boundary = boundary;
    }

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<OrderReply> responseObserver) {
        OrderResult result = service.create(request.getOrderId(), request.getCustomerId(), request.getProduct());
        if (result.outcome().isFailed()) {
            responseObserver.onError(boundary.toException(result.outcome()));
            return;
        }
        responseObserver.onNext(toReply(result.value()));
        responseObserver.onCompleted();
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<OrderReply> responseObserver) {
        OrderResult result = service.find(request.getOrderId());
        if (result.outcome().isFailed()) {
            responseObserver.onError(boundary.toException(result.outcome()));
            return;
        }
        responseObserver.onNext(toReply(result.value()));
        responseObserver.onCompleted();
    }

    private static OrderReply toReply(OrderRecord order) {
        return OrderReply.newBuilder()
            .setOrderId(order.orderId())
            .setCustomerId(order.customerId())
            .setProduct(order.product())
            .setStatus(order.status())
            .build();
    }
}
