package com.example.martrust;

import com.example.martrust.service.CurrencyExchangeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.ValidationException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class CurrencyExchangeServiceTest {
    private MockRestServiceServer mockServer;

    @Mock
    private RestTemplate restTemplate;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @InjectMocks
    private CurrencyExchangeService currencyExchangeService = new CurrencyExchangeService();

    Map<String, String> SYMBOL_MAP = new HashMap<>();
    Map<String, Double> RATES_MAP = new HashMap<>();

    final CurrencyExchangeDTO DTO_SYMBOLS = CurrencyExchangeDTO.builder()
            .symbols(SYMBOL_MAP)
            .success(true)
            .build();

    final CurrencyExchangeDTO DTO_RATES = CurrencyExchangeDTO.builder()
            .rates(RATES_MAP)
            .success(true)
            .build();

    CurrencyExchangeDTO EXCHANGE_DTO = CurrencyExchangeDTO.builder()
            .success(true)
            .build();
    @Before
    public void setUp() {

        //Symbol map initialization
        SYMBOL_MAP.put("USD", "US Dollar");
        SYMBOL_MAP.put("PHP", "Philippine Peso");
        SYMBOL_MAP.put("EUR", "British Pound");

        //Rate map initialization
        RATES_MAP.put("USD", 1.21);
        RATES_MAP.put("PHP", 58.60);
        RATES_MAP.put("EUR", 1.0);

        when(restTemplate.getForObject(contains("symbols"), eq(CurrencyExchangeDTO.class))).thenReturn(DTO_SYMBOLS);
        when(restTemplate.getForObject(contains("base"), eq(CurrencyExchangeDTO.class))).thenReturn(DTO_RATES);
    }

    @Test
    public void test_getCurrencySymbolMap_ok() throws Exception {
        Map<String, String> currencyMap = currencyExchangeService.getCurrencySymbolMap();

        assertEquals(currencyMap.size(), 3);
        assertEquals(currencyMap.get("PHP"), "Philippine Peso");
        assertTrue(currencyMap.containsValue("British Pound"));

    }

    @Test(expected = Exception.class)
    public void test_getCurrencySymbolMap_error() throws Exception {
        DTO_SYMBOLS.setSuccess(false);

        currencyExchangeService.getCurrencySymbolMap();
    }

    @Test
    public void test_getExchangeConversion_ok() throws Exception{
        EXCHANGE_DTO = CurrencyExchangeDTO.builder()
                .buyCurrency("USD")
                .buyAmount(1)
                .sellCurrency("PHP")
                .build();

        currencyExchangeService.getExchangeConversion(EXCHANGE_DTO);

        assertEquals(Math.round(EXCHANGE_DTO.getSellAmount() * 100.0) / 100.0, 48.43, 2);

        EXCHANGE_DTO.setBuyAmount(0.0);
        EXCHANGE_DTO.setSellAmount(2048.50);

        currencyExchangeService.getExchangeConversion(EXCHANGE_DTO);

        assertEquals(Math.round(EXCHANGE_DTO.getBuyAmount() * 100.0 ) / 100.0, 42.30, 2);
    }

    @Test
    public void test_getExchangeConversion_validationError () throws Exception {
        EXCHANGE_DTO = CurrencyExchangeDTO.builder()
                .buyCurrency("NON_EXISTENT_CURRENCY")
                .buyAmount(1)
                .sellCurrency("PHP")
                .build();

        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Buy/Sell currency is null or does not exist within the database.");

        currencyExchangeService.getExchangeConversion(EXCHANGE_DTO);

        EXCHANGE_DTO.setBuyCurrency("USD");
        EXCHANGE_DTO.setBuyAmount(-1.0);

        expectedEx.expectMessage("Negative numbers are not allowed in this transaction.");
        currencyExchangeService.getExchangeConversion(EXCHANGE_DTO);
    }
}
