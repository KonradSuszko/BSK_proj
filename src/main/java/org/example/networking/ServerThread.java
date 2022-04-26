package org.example.networking;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@AllArgsConstructor
public class ServerThread extends Thread {

    private int port;
    private SocketBoard board;
    private ServerSocket ss;
    private Socket s;

    public ServerThread(int port, SocketBoard board){
        this.port = port;
        this.board = board;
    }

    public void kill() throws IOException{
        this.interrupt();
        if (s != null)
            s.close();
        if (ss != null)
            ss.close();
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(port);
            while (!Thread.interrupted()) {
                accept(ss);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(ServerSocket ss) throws IOException {
        try {
            s = ss.accept();
            System.out.println("new connection : " + s);
            board.put(s);

        } catch (Exception e) {
            assert s != null;
            if (s != null)
                s.close();
            e.printStackTrace();
        }
    }
}
