package org.example.networking;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@AllArgsConstructor
public class ServerThread extends Thread {

    private int port;
    private SocketBoard board;


    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)){

            while (!Thread.interrupted()) {
                accept(ss);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(ServerSocket ss) throws IOException {
        Socket s = null;
        try {
            s = ss.accept();
            System.out.println("new connection : " + s);
            board.put(s);

        } catch (Exception e) {
            assert s != null;
            s.close();
            e.printStackTrace();
        }
    }
}
