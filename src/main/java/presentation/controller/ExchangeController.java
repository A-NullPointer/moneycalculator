package presentation.controller;

import application.usecase.ExchangeMoneyUseCase;
import domain.exception.ExchangeRateException;
import domain.model.Currency;
import domain.model.Money;
import presentation.ui.MoneyCalculatorView;

public class ExchangeController {
    private final ExchangeMoneyUseCase exchangeMoneyUseCase;
    private MoneyCalculatorView view;

    public ExchangeController(ExchangeMoneyUseCase exchangeMoneyUseCase) {
        this.exchangeMoneyUseCase = exchangeMoneyUseCase;
    }

    public void setView(MoneyCalculatorView view) {
        this.view = view;
    }

    public Money exchangeMoney(Money money, Currency targetCurrency) {
        try {
            return exchangeMoneyUseCase.execute(money, targetCurrency);
        } catch (ExchangeRateException e) {
            throw new RuntimeException("Failed to get exchange rate: " + e.getMessage(), e);
        }
    }

    public void handleExchange() {
        if (view == null) {
            throw new IllegalStateException("View not set");
        }

        try {
            Money inputMoney = view.getInputMoney();
            Currency targetCurrency = view.getTargetCurrency();
            
            Money result = exchangeMoney(inputMoney, targetCurrency);
            view.displayResult(result);
            
        } catch (NumberFormatException e) {
            view.displayError("Please enter a valid amount");
        } catch (ExchangeRateException e) {
            view.displayError("Could not fetch exchange rate. Please try again.");
        } catch (Exception e) {
            view.displayError("An error occurred: " + e.getMessage());
        }
    }
}
