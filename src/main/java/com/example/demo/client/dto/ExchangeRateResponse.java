package com.example.demo.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        LocalDate asOfDate,
        boolean isFallback
) {
    public static ExchangeRateResponse fallback(String baseCurrency, String targetCurrency) {
        return new ExchangeRateResponse(
                baseCurrency,
                targetCurrency,
                BigDecimal.ONE,
                LocalDate.now(),
                true
        );
    }
}
