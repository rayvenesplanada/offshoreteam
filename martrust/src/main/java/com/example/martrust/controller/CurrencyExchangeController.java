package com.example.martrust.controller;

import com.example.martrust.CurrencyExchangeDTO;
import com.example.martrust.service.CurrencyExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/currency_exchange")
public class CurrencyExchangeController {

    @Autowired
    CurrencyExchangeService currencyExchangeService;

    @GetMapping
    public Map<String, String> getCurrencySymbols() throws Exception{
        return currencyExchangeService.getCurrencySymbolMap();
    }

    @GetMapping("/convert")
    public CurrencyExchangeDTO convertCurrency(@RequestBody CurrencyExchangeDTO exchangeDTO) throws Exception{
        return currencyExchangeService.getExchangeConversion(exchangeDTO);
    }
}
