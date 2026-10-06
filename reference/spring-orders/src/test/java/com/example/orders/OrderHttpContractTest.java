package com.example.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderHttpContractTest {

    @Test
    void mvcRendersProblemDetailContract() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController())
            .setControllerAdvice(new OrderProblemHandler())
            .build();

        mvc.perform(get("/orders/o-123").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.example.com/problems/order-not-found"))
            .andExpect(jsonPath("$.title").value("Order not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Order o-123 was not found."));
    }

    @Test
    void webFluxRendersProblemDetailContract() {
        WebTestClient client = WebTestClient.bindToController(new OrderController())
            .controllerAdvice(new OrderProblemHandler())
            .build();

        client.get()
            .uri("/orders/o-123")
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://api.example.com/problems/order-not-found")
            .jsonPath("$.title").isEqualTo("Order not found")
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.detail").isEqualTo("Order o-123 was not found.");
    }
}
