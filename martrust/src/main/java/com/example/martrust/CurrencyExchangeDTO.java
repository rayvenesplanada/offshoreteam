package com.example.martrust;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CurrencyExchangeDTO {

    private boolean success;

    private Map<String, String> symbols;
    private Map<String, Double> rates;

    private String buyCurrency;
    private double buyAmount;
    private String sellCurrency;
    private double sellAmount;

    private double baseConversionAmount;

}
