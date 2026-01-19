package application.port;

import domain.model.Currency;
import domain.model.ExchangeRate;

public interface ExchangeRateRepository {
    ExchangeRate getExchangeRate(Currency from, Currency to);
}
