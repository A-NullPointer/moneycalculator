package domain.service;

import application.port.CurrencyRepository;
import domain.exception.CurrencyNotFoundException;
import domain.model.Currency;

import java.util.List;

public class CurrencyService {
    private final CurrencyRepository repository;
    private List<Currency> cachedCurrencies;

    public CurrencyService(CurrencyRepository repository) {
        this.repository = repository;
    }

    public List<Currency> getAllCurrencies() {
        if (cachedCurrencies == null) {
            cachedCurrencies = repository.findAll();
        }
        return cachedCurrencies;
    }

    public Currency getCurrency(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException(code));
    }

    public void refreshCache() {
        cachedCurrencies = null;
    }
}
