package org.example.ui.threading;

import java.util.ArrayList;
import java.util.List;

public class MessageBoard {
    private final List<String> messages = new ArrayList<>();

    public synchronized String take() throws InterruptedException {
        while (messages.isEmpty()) {
            wait();
        }
        return messages.remove(0);
    }

    public synchronized void put(String s) {
        messages.add(s);
        notifyAll();
    }
}
