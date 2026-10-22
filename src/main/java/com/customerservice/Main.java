package com.customerservice;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println(
                    "Using default Swing appearance.");
            }

            CustomerServiceGUI gui = new CustomerServiceGUI();
            gui.setVisible(true);
        });
    }
}