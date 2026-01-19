package domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(double amount, Currency currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    public Money exchange(ExchangeRate rate) {
        if (!this.currency.equals(rate.from())) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate.rate()));
        return new Money(convertedAmount, rate.to());
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency.code();
    }
}
