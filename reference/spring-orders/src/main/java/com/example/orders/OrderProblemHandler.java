package com.example.orders;

import io.github.aalsanie.codes.spring.ProblemDetails;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class OrderProblemHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFound(OrderNotFoundException exception) {
        return ProblemDetails.forTypeAndDetail(
            OrderProblems.ORDER_NOT_FOUND,
            "Order " + exception.orderId() + " was not found."
        );
    }

    @ExceptionHandler(ArchivedOrderNotFoundException.class)
    ProblemDetail handleArchivedOrderNotFound(ArchivedOrderNotFoundException exception) {
        return ProblemDetails.forTypeAndDetail(
            OrderProblems.ORDER_NOT_FOUND,
            "Archived order " + exception.orderId() + " was not found."
        );
    }
}
