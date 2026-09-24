package com.abelium.inatrace.components.currencies;

import com.abelium.inatrace.components.codebook.currencies.CurrencyTypeService;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.currencies.api.ApiCurrencyRatesResponse;
import com.abelium.inatrace.db.entities.codebook.CurrencyType;
import com.abelium.inatrace.db.entities.currencies.CurrencyPair;
import com.abelium.inatrace.tools.SimpleCircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private static final String CURRENCY = "currency";

    private final SimpleCircuitBreaker circuitBreaker = new SimpleCircuitBreaker("ExchangeRatesHistoricalApi");

    @Autowired
    private CurrencyTypeService currencyTypeService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${INAtrace.exchangerate.apiKey}")
    private String apiKey;

    @Value("${INAtrace.exchangerate.enabled:true}")
    private boolean exchangerateEnabled;

    public BigDecimal convertFromEur(String to, BigDecimal value) {
        return value.multiply(em.createNamedQuery("CurrencyPair.latestRate", BigDecimal.class).setParameter(CURRENCY, to).getResultList().get(0));
    }

    public BigDecimal convertToEur(String from, BigDecimal value) {
        return value.divide(em.createNamedQuery("CurrencyPair.latestRate", BigDecimal.class).setParameter(CURRENCY, from).getResultList().get(0), 6, RoundingMode.HALF_UP);
    }

    @Transactional
    public BigDecimal convertFromEurAtDate(String to, BigDecimal value, Date date) {

        List<BigDecimal> rates = rateAtDateQuery(to, date).getResultList();
        BigDecimal rate;
        if (rates.isEmpty()) {
            fetchRates(date);
            List<BigDecimal> newRates = rateAtDateQuery(to, date).getResultList();
            if (newRates.isEmpty()) {
                log.warn("No exchange rate available for {} at date {}, attempting fallback to latest rate", to, date);
                return convertFromEur(to, value);
            }
            rate = newRates.get(0);
        } else {
            rate = rates.get(0);
        }
        return value.multiply(rate);
    }

    @Transactional
    public BigDecimal convertToEurAtDate(String from, BigDecimal value, Date date) {

        List<BigDecimal> rates = rateAtDateQuery(from, date).getResultList();
        BigDecimal rate;
        if (rates.isEmpty()) {
            fetchRates(date);
            List<BigDecimal> newRates = rateAtDateQuery(from, date).getResultList();
            if (newRates.isEmpty()) {
                log.warn("No exchange rate available for {} at date {}, attempting fallback to latest rate", from, date);
                return convertToEur(from, value);
            }
            rate = newRates.get(0);
        } else {
            rate = rates.get(0);
        }
        return value.divide(rate, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal convert(String from, String to, BigDecimal value) {

        if (from.equals(to)) {
            return value;
        } else if ("EUR".equals(from)) {
            return this.convertFromEur(to, value);
        } else if ("EUR".equals(to)) {
            return this.convertToEur(from, value);
        } else {
            return this.convertFromEur(to, this.convertToEur(from, value));
        }
    }

    @Transactional
    public BigDecimal convertAtDate(String from, String to, BigDecimal value, Date date) {

        if (from.equals(to)) {
            return value;
        } else if ("EUR".equals(from)) {
            return this.convertFromEurAtDate(to, value, date);
        } else if ("EUR".equals(to)) {
            return this.convertToEurAtDate(from, value, date);
        } else {
            return this.convertFromEurAtDate(to, this.convertToEurAtDate(from, value, date), date);
        }
    }

    public void fetchRates(Date date) {

        if (!exchangerateEnabled) {
            log.info("Exchange rate synchronization is disabled by configuration (INATrace.exchangerate.enabled=false).");
            return;
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your_api_key_here") || apiKey.equals("dummy-test-key") || apiKey.equalsIgnoreCase("disabled")) {
            log.warn("Exchange rates API key is not configured ({}). Skipping rate fetch for date.", apiKey);
            return;
        }

        String isoDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date.toInstant().atZone(ZoneId.of("GMT")));

        ApiCurrencyRatesResponse apiCurrencyRatesResponse = circuitBreaker.execute(() -> {
            return webClientBuilder.build()
                    .get()
                    .uri("https://api.exchangeratesapi.io/v1/" + isoDate + "?access_key=" + apiKey + "&base=EUR")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ApiCurrencyRatesResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        }, () -> {
            log.warn("Fallback: unable to fetch historical currency rates for date {}, returning empty response", isoDate);
            return new ApiCurrencyRatesResponse();
        });

        if (apiCurrencyRatesResponse != null && apiCurrencyRatesResponse.isSuccess()) {
            Map<String, BigDecimal> rates = apiCurrencyRatesResponse.getRates();
            Date current = apiCurrencyRatesResponse.getDate();

            List<String> enabled = currencyTypeService.getEnabledCurrencyCodes();

            for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
                if (enabled.contains(entry.getKey()) && rateAtDateQuery(entry.getKey(), current).getResultList().isEmpty()) {
                    CurrencyPair currencyPair = new CurrencyPair();
                    CurrencyType from = currencyTypeService.getCurrencyTypeByCode("EUR");
                    CurrencyType to = currencyTypeService.getCurrencyTypeByCode(entry.getKey());
                    currencyPair.setFrom(from);
                    currencyPair.setTo(to);
                    currencyPair.setDate(current);
                    currencyPair.setValue(entry.getValue());
                    em.persist(currencyPair);
                }
            }
        }
    }

    private TypedQuery<BigDecimal> rateAtDateQuery(String currency, Date date) {
        return em.createNamedQuery("CurrencyPair.rateAtDate", BigDecimal.class).setParameter(CURRENCY, currency).setParameter("date", date);
    }

}
