package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

class SpringStackCompatibilityContractTest {
    @Test
    void mvcResponseEntityExceptionHandlerAcceptsTheAdapterBridgeType() throws Exception {
        assertErrorResponseHook(
            org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler.class,
            WebRequest.class
        );
    }

    @Test
    void webFluxResponseEntityExceptionHandlerAcceptsTheAdapterBridgeType() throws Exception {
        assertErrorResponseHook(
            org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler.class,
            ServerWebExchange.class
        );
    }

    private static void assertErrorResponseHook(
        Class<?> handlerType,
        Class<?> requestType
    ) throws NoSuchMethodException {
        Method method = handlerType.getDeclaredMethod(
            "handleErrorResponseException",
            ErrorResponseException.class,
            HttpHeaders.class,
            HttpStatusCode.class,
            requestType
        );

        assertTrue(Modifier.isProtected(method.getModifiers()));
    }
}
