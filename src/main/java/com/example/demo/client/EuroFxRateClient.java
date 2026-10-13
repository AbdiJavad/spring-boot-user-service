package com.example.demo.client;

import com.example.demo.client.dto.ExchangeRateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EuroFxRateClient {

    private static final Logger log = LoggerFactory.getLogger(EuroFxRateClient.class);
    public static final String RESILIENCE_INSTANCE = "euroFxClient";

    private RestClient restClient;

    public EuroFxRateClient(
            RestClient.Builder restClientBuilder,
            @Value("${client.eurofx.base-url:https://api.frankfurter.app}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    // Allows re-binding during MockRestServiceServer tests if needed
    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "getExchangeRateFallback")
    public ExchangeRateResponse getExchangeRate(String targetCurrency) {
        log.info("Fetching exchange rate for EUR to {}", targetCurrency);
        return restClient.get()
                .uri("/latest?from=EUR&to={targetCurrency}", targetCurrency)
                .retrieve()
                .body(ExchangeRateResponse.class);
    }

    public ExchangeRateResponse getExchangeRateFallback(String targetCurrency, Throwable throwable) {
        log.warn("Circuit breaker fallback activated for currency: {}. Reason: {}", 
                targetCurrency, throwable.getMessage());
        return ExchangeRateResponse.fallback("EUR", targetCurrency);
    }
}
