package application.usecase;

import application.port.ExchangeRateRepository;
import domain.model.Currency;
import domain.model.ExchangeRate;
import domain.model.Money;

public class ExchangeMoneyUseCase {
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeMoneyUseCase(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Money execute(Money money, Currency targetCurrency) {
        ExchangeRate rate = exchangeRateRepository.getExchangeRate(
                money.currency(), 
                targetCurrency
        );
        return money.exchange(rate);
    }
}
