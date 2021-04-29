package com.example.martrust.service;

import com.example.martrust.CurrencyExchangeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.ValidationException;
import java.util.Map;
import java.util.Optional;

@Service
public class CurrencyExchangeService {
    @Value("${exchange.rate.access.key}")
    private String ACCESS_KEY;
    @Value("${exchange.rate.base.uri}")
    private String BASE_URI;


    @Autowired
    RestTemplate restTemplate;

    public Map<String, String> getCurrencySymbolMap() throws Exception {
        String getPath = BASE_URI + "/symbols?access_key=" + ACCESS_KEY;
        CurrencyExchangeDTO exchangeDTO = restTemplate
                .getForObject(getPath, CurrencyExchangeDTO.class);
        if (exchangeDTO.isSuccess()) {
            return exchangeDTO.getSymbols();
        } else {
            throw new Exception("Failed to retrieve records.");
        }
    }

    public CurrencyExchangeDTO getExchangeConversion(CurrencyExchangeDTO exchangeDTO) throws Exception {
        Map<String, String> currencySymbolMap = getCurrencySymbolMap();

        if ((exchangeDTO.getBuyCurrency() == null || currencySymbolMap.get(exchangeDTO.getBuyCurrency()) == null)
                || (exchangeDTO.getSellCurrency() == null || currencySymbolMap.get(exchangeDTO.getSellCurrency()) == null)) {
            throw new ValidationException("Buy/Sell currency is null or does not exist within the database.");
        }

        if (exchangeDTO.getBuyAmount() < 0 || exchangeDTO.getSellAmount() < 0) {
            throw new ValidationException("Negative numbers are not allowed in this transaction.");
        }

        String getPath = BASE_URI + "/latest?access_key=" + ACCESS_KEY +
                "&base=EUR&symbols=" + exchangeDTO.getSellCurrency() + "," + exchangeDTO.getBuyCurrency();

        Map<String, Double> rates = Optional.ofNullable(restTemplate.getForObject(getPath, CurrencyExchangeDTO.class).getRates())
                .orElseThrow(() -> new RuntimeException("Error in retrieving from API."));

        exchangeDTO.setBaseConversionAmount(computeConversionAmount(rates.get(exchangeDTO.getBuyCurrency()),
                rates.get(exchangeDTO.getSellCurrency())));
        exchangeDTO.setSuccess(true);

        if (exchangeDTO.getBuyAmount() > 0) {
            exchangeDTO.setSellAmount(exchangeDTO.getBuyAmount() * exchangeDTO.getBaseConversionAmount());
        } else {

            exchangeDTO.setBuyAmount(exchangeDTO.getSellAmount() / exchangeDTO.getBaseConversionAmount());
        }

        return exchangeDTO;
    }

    private static Double computeConversionAmount(Double buyCurrencyRate, Double sellCurrencyRate) {
        Double fromEURRate = 1 / buyCurrencyRate;

        return sellCurrencyRate * fromEURRate;
    }
}
