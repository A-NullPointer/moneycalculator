package domain.model;

import java.time.LocalDate;

public class ExchangeRate {
    private final LocalDate date;
    private final Currency from;
    private final Currency to;
    private final double rate;

    public ExchangeRate(LocalDate date, Currency from, Currency to, double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
        this.date = date;
        this.from = from;
        this.to = to;
        this.rate = rate;
    }

    public LocalDate date() { return date; }
    public Currency from() { return from; }
    public Currency to() { return to; }
    public double rate() { return rate; }
}
