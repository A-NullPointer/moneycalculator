package presentation.ui;

import domain.model.Money;

import javax.swing.*;
import java.awt.*;

public class MoneyDisplayPanel extends JPanel {
    private JLabel resultLabel;
    private JLabel detailLabel;

    public MoneyDisplayPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createTitledBorder("Result")
        ));
        
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 32));
        resultLabel.setForeground(new Color(33, 150, 243));
        
        detailLabel = new JLabel("", SwingConstants.CENTER);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailLabel.setForeground(Color.GRAY);
        
        add(resultLabel, BorderLayout.CENTER);
        add(detailLabel, BorderLayout.SOUTH);
    }

    public void show(Money money) {
        resultLabel.setText(String.format("%.2f %s", 
                money.amount().doubleValue(), 
                money.currency().code()));
        detailLabel.setText(money.currency().name());
    }

    public void clear() {
        resultLabel.setText("");
        detailLabel.setText("");
    }
}
