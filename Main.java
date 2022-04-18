import networking.CommunicationHandler;
import networking.ServerThread;
import networking.User;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


public class Main
{
    public static void main(String[] args) throws IOException
    {
        Scanner scn = new Scanner(System.in);
        System.out.print("Port: ");
        int port = scn.nextInt();
        ServerThread st = new ServerThread(port);
        st.start();
        System.out.print("Port to connect to: ");
        int toConnect = scn.nextInt();
        User user = new User();
        user.connect(toConnect);

    }
}