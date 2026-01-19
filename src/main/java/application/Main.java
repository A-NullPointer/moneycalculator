package application;

import application.usecase.ExchangeMoneyUseCase;
import domain.service.CurrencyService;
import domain.service.ExchangeRateService;
import infrastructure.api.ExchangeRateApiClient;
import presentation.controller.ExchangeController;
import presentation.ui.MoneyCalculatorView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExchangeRateApiClient apiClient = new ExchangeRateApiClient();
            
            CurrencyService currencyService = new CurrencyService(apiClient);
            ExchangeRateService exchangeRateService = new ExchangeRateService(apiClient);
            
            ExchangeMoneyUseCase exchangeMoneyUseCase = new ExchangeMoneyUseCase(apiClient);
            
            ExchangeController controller = new ExchangeController(exchangeMoneyUseCase);

            MoneyCalculatorView view = new MoneyCalculatorView();
            
            initializeApplication(view, controller, currencyService);
            
            view.setVisible(true);
        });
    }

    private static void initializeApplication(
            MoneyCalculatorView view, 
            ExchangeController controller,
            CurrencyService currencyService) {
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                view.setCurrencies(currencyService.getAllCurrencies());
                return null;
            }

            @Override
            protected void done() {
                view.onExchangeClicked(() -> performExchange(view, controller));
            }
        }.execute();
    }

    private static void performExchange(MoneyCalculatorView view, ExchangeController controller) {
        view.showLoading(true);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                var money = view.getInputMoney();
                var targetCurrency = view.getTargetCurrency();
                var result = controller.exchangeMoney(money, targetCurrency);
                view.displayResult(result);
                return null;
            }

            @Override
            protected void done() {
                view.showLoading(false);
            }
        }.execute();
    }
}
