package domain.model;

import java.util.Objects;

public class Currency {
    private final String code;
    private final String name;

    public Currency(String code, String name) {
        this.code = validateCode(code);
        this.name = Objects.requireNonNull(name, "Currency name cannot be null");
    }

    private String validateCode(String code) {
        if (code == null || code.length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 characters");
        }
        return code.toUpperCase();
    }

    public String code() { return code; }
    public String name() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency currency)) return false;
        return code.equals(currency.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() { return code + " - " + name; }
}
