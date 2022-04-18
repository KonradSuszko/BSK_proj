package org.example;

import org.example.ui.LoginView;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        try {
            LoginView login = new LoginView();
            login.setSize(350, 250);
            login.setVisible(true);
            login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            login.setLocationRelativeTo(null);
        } catch (Exception e) {
            System.exit(0);
        }
    }
}