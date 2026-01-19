package domain.service;

import application.port.ExchangeRateRepository;
import domain.model.Currency;
import domain.model.ExchangeRate;

import java.util.HashMap;
import java.util.Map;

public class ExchangeRateService {
    private final ExchangeRateRepository repository;
    private final Map<String, ExchangeRate> cache;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
        this.cache = new HashMap<>();
    }

    public ExchangeRate getRate(Currency from, Currency to) {
        String key = from.code() + "-" + to.code();
        
        if (cache.containsKey(key)) {
            ExchangeRate cached = cache.get(key);
            if (isValid(cached)) {
                return cached;
            }
        }
        
        ExchangeRate rate = repository.getExchangeRate(from, to);
        cache.put(key, rate);
        return rate;
    }

    private boolean isValid(ExchangeRate rate) {
        return rate.date().equals(java.time.LocalDate.now());
    }

    public void clearCache() {
        cache.clear();
    }
}
