package infrastructure.api;

import com.google.gson.Gson;
import application.port.CurrencyRepository;
import application.port.ExchangeRateRepository;
import domain.exception.ExchangeRateException;
import domain.model.Currency;
import domain.model.ExchangeRate;
import infrastructure.api.dto.CurrencyListResponse;
import infrastructure.api.dto.ExchangeRateResponse;
import infrastructure.config.ApiConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateApiClient implements ExchangeRateRepository, CurrencyRepository {
    private final Gson gson;
    private static final int TIMEOUT = 5000;

    public ExchangeRateApiClient() {
        this.gson = new Gson();
    }

    @Override
    public ExchangeRate getExchangeRate(Currency from, Currency to) {
        try {
            String json = fetchData(ApiConfig.getPairUrl(from.code(), to.code()));
            ExchangeRateResponse response = gson.fromJson(json, ExchangeRateResponse.class);
            
            if (!"success".equals(response.getResult())) {
                throw new ExchangeRateException("API returned error for " + from.code() + " to " + to.code());
            }
            
            return new ExchangeRate(LocalDate.now(), from, to, response.getConversionRate());
        } catch (IOException e) {
            throw new ExchangeRateException("Failed to fetch exchange rate", e);
        }
    }

    @Override
    public List<Currency> findAll() {
        try {
            String json = fetchData(ApiConfig.getCodesUrl());
            CurrencyListResponse response = gson.fromJson(json, CurrencyListResponse.class);
            
            return response.getSupportedCodes().stream()
                    .map(tuple -> new Currency(tuple.get(0), tuple.get(1)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ExchangeRateException("Failed to fetch currencies", e);
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        return findAll().stream()
                .filter(currency -> currency.code().equalsIgnoreCase(code))
                .findFirst();
    }

    private String fetchData(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        try (InputStream is = connection.getInputStream()) {
            return new String(is.readAllBytes());
        } finally {
            connection.disconnect();
        }
    }
}
