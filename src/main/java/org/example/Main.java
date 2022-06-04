package org.example;

import org.example.cryptography.RSAUtils;
import org.example.ui.LoginView;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        try {
            String destPath = args[0];
            String privateKeyPath = args[2];
            String publicKeyPath = args[1];
            RSAUtils.initialize(privateKeyPath, publicKeyPath, "haslo");
            LoginView login = new LoginView(destPath);
            login.setSize(350, 250);
            login.setVisible(true);
            login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            login.setLocationRelativeTo(null);
        } catch (Exception e) {
            System.exit(0);
        }
    }
}