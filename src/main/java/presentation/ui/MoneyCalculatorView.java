package presentation.ui;

import domain.model.Currency;
import domain.model.Money;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MoneyCalculatorView extends JFrame {
    private JTextField amountField;
    private JComboBox<Currency> fromCurrencyCombo;
    private JComboBox<Currency> toCurrencyCombo;
    private JButton exchangeButton;
    private MoneyDisplayPanel resultPanel;

    public MoneyCalculatorView() {
        setTitle("Money Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        amountField = new JTextField(10);
        fromCurrencyCombo = new JComboBox<>();
        toCurrencyCombo = new JComboBox<>();
        exchangeButton = new JButton("Exchange");
        resultPanel = new MoneyDisplayPanel();
        
        // Styling
        amountField.setFont(new Font("Arial", Font.PLAIN, 16));
        fromCurrencyCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        toCurrencyCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        exchangeButton.setFont(new Font("Arial", Font.BOLD, 16));
        exchangeButton.setBackground(new Color(76, 175, 80));
        exchangeButton.setForeground(Color.WHITE);
        exchangeButton.setFocusPainted(false);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(amountField, gbc);

        // From Currency
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(fromCurrencyCombo, gbc);

        // To Currency
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(toCurrencyCombo, gbc);

        // Exchange Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.ipady = 10;
        inputPanel.add(exchangeButton, gbc);

        add(inputPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
    }

    public void setCurrencies(List<Currency> currencies) {
        fromCurrencyCombo.removeAllItems();
        toCurrencyCombo.removeAllItems();
        
        for (Currency currency : currencies) {
            fromCurrencyCombo.addItem(currency);
            toCurrencyCombo.addItem(currency);
        }
    }

    public Money getInputMoney() {
        double amount = Double.parseDouble(amountField.getText());
        Currency currency = (Currency) fromCurrencyCombo.getSelectedItem();
        return new Money(amount, currency);
    }

    public Currency getTargetCurrency() {
        return (Currency) toCurrencyCombo.getSelectedItem();
    }

    public void displayResult(Money result) {
        resultPanel.show(result);
    }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void onExchangeClicked(Runnable action) {
        exchangeButton.addActionListener(e -> {
            try {
                action.run();
            } catch (NumberFormatException ex) {
                displayError("Please enter a valid number");
            } catch (Exception ex) {
                displayError("Error: " + ex.getMessage());
            }
        });
    }

    public void showLoading(boolean loading) {
        exchangeButton.setEnabled(!loading);
        exchangeButton.setText(loading ? "Loading..." : "Exchange");
    }
}
