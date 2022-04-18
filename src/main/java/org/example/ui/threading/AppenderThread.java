package org.example.ui.threading;

import lombok.AllArgsConstructor;

import javax.swing.*;

@AllArgsConstructor
public class AppenderThread implements Runnable {
    MessageBoard board;
    JTextArea chatArea;


    @Override
    public void run() {
        while (true) {
            try {
                String message = board.take();
                chatArea.append("\n\t\t\t\t [other guy]: " + message);
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
