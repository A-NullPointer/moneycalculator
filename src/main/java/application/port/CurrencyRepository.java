package application.port;

import domain.model.Currency;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {
    List<Currency> findAll();
    Optional<Currency> findByCode(String code);
}
