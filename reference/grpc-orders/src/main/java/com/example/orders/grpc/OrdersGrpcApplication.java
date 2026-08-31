package com.example.orders.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;

public final class OrdersGrpcApplication {
    private OrdersGrpcApplication() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = NettyServerBuilder.forPort(9090)
            .addService(new OrdersGrpcService(new OrderService(), new OutcomeGrpcBoundary()))
            .build()
            .start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
