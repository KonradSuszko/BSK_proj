package org.example.ui.threading;

import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;

@AllArgsConstructor
public class ListenerThread implements Runnable {
    DataInputStream readStream;
    MessageBoard board;

    @Override
    public void run() {
        while (true) {
            try {
                String message = readStream.readUTF();
                board.put(message);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
