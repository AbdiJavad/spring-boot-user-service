package com.example.demo.client;

import com.example.demo.client.dto.ExchangeRateResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
class EuroFxRateClientIntegrationTest {

    @Autowired
    private EuroFxRateClient euroFxRateClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private MockRestServiceServer mockServer;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.frankfurter.app");
        mockServer = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        euroFxRateClient.setRestClient(builder.build());

        circuitBreaker = circuitBreakerRegistry.circuitBreaker(EuroFxRateClient.RESILIENCE_INSTANCE);
        circuitBreaker.reset();
    }

    @Test
    @DisplayName("Should return successful response and keep Circuit Breaker in CLOSED state")
    void shouldReturnSuccessfulRateWhenRemoteServiceIsAvailable() {
        String mockResponseBody = """
                {
                    "baseCurrency": "EUR",
                    "targetCurrency": "USD",
                    "rate": 1.0850,
                    "asOfDate": "2026-10-12",
                    "isFallback": false
                }
                """;

        mockServer.expect(requestTo("https://api.frankfurter.app/latest?from=EUR&to=USD"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        ExchangeRateResponse response = euroFxRateClient.getExchangeRate("USD");

        mockServer.verify();
        assertThat(response).isNotNull();
        assertThat(response.baseCurrency()).isEqualTo("EUR");
        assertThat(response.targetCurrency()).isEqualTo("USD");
        assertThat(response.rate()).isEqualByComparingTo(new BigDecimal("1.0850"));
        assertThat(response.isFallback()).isFalse();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("Should invoke fallback and transition to OPEN state after threshold failures")
    void shouldOpenCircuitBreakerWhenThresholdExceeded() {
        mockServer.expect(ExpectedCount.times(5), requestTo("https://api.frankfurter.app/latest?from=EUR&to=GBP"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        for (int i = 0; i < 5; i++) {
            ExchangeRateResponse response = euroFxRateClient.getExchangeRate("GBP");
            assertThat(response).isNotNull();
            assertThat(response.isFallback()).isTrue();
            assertThat(response.rate()).isEqualTo(BigDecimal.ONE);
        }

        mockServer.verify();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
