package org.example.ui.threading;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class KeyBoard {
    private final List<PublicKey> list = new ArrayList<>();

    public synchronized PublicKey take() throws InterruptedException {
        while (list.isEmpty()) {
            wait();
        }
        return list.remove(0);
    }

    public synchronized void put(PublicKey key) {
        list.add(key);
        notifyAll();
    }
}
