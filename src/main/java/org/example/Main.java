package org.example;

import org.example.ui.LoginView;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        try {
            String destPath = args[0];
            String privateKeyPath = args[2];
            String publicKeyPath = args[1];
            LoginView login = new LoginView(destPath, privateKeyPath, publicKeyPath);
            login.setSize(350, 300);
            login.setVisible(true);
            login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            login.setLocationRelativeTo(null);
        } catch (Exception e) {
            System.exit(0);
        }
    }
}