package org.example.networking;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketBoard {
    private final List<Socket> list = new ArrayList<>();

    public synchronized Socket take() throws InterruptedException {
        while (list.isEmpty()) {
            wait();
        }
        return list.remove(0);
    }

    public synchronized void put(Socket s) {
        list.add(s);
        notifyAll();
    }
}
