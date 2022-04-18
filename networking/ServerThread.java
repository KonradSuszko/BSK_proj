package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private int port;

    public ServerThread(int port){
        this.port = port;
    }


    @Override
    public void run() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);

            while (true) {
                Socket s = null;

                // petla do akceptowania polaczen
                try {
                    s = ss.accept();
                    System.out.println("new connection : " + s);

                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                    System.out.println("Assigning new thread");
                    Thread t = new CommunicationHandler(s, dis, dos);
                    t.start();

                } catch (Exception e) {
                    s.close();
                    e.printStackTrace();
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}
