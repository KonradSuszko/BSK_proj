package org.example.ui;

import org.example.networking.ServerThread;
import org.example.networking.SocketBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class LoginView extends JFrame implements ActionListener {
    final Container container = getContentPane();
    final JLabel myPortLabel = new JLabel("MY PORT");
    final JLabel otherPortLabel = new JLabel("OTHER PORT");
    final JTextField myPortField = new JTextField();
    final JTextField otherPortField = new JTextField();
    final JButton connectButton = new JButton("CONNECT");
    private final String destPath;

    public LoginView(String destPath) throws HeadlessException {
        setTitle("Connection screen");
        container.setLayout(null);
        setLocationAndSize();
        addComponentsToContainer();
        connectButton.addActionListener(this);
        this.destPath = destPath;
    }

    private static @NotNull Socket getSocket(int port) throws IOException {
        InetAddress ip = InetAddress.getByName("localhost");
        return new Socket(ip, port);
    }

    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource() == connectButton) {
            try {
                int myPort = Integer.parseInt(myPortField.getText());
                int otherPort = Integer.parseInt(otherPortField.getText());
                SocketBoard board = new SocketBoard();
                ServerThread serverThread = new ServerThread(myPort, board);
                serverThread.start();
                Socket clientSocket = getClientSocket(otherPort, serverThread);
                Socket serverSocket = board.take();
                serverThread.interrupt();
                initChat(clientSocket, serverSocket);
            } catch (IllegalThreadStateException exception) {
                JOptionPane.showMessageDialog(this, "Something wrong I can feel it");
            } catch (InterruptedException exception){
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setLocationAndSize() {
        myPortLabel.setBounds(50, 50, 100, 30);
        otherPortLabel.setBounds(50, 100, 100, 30);
        myPortField.setBounds(150, 50, 150, 30);
        otherPortField.setBounds(150, 100, 150, 30);
        connectButton.setBounds(200, 150, 100, 30);
    }

    private void addComponentsToContainer() {
        container.add(myPortLabel);
        container.add(otherPortLabel);
        container.add(myPortField);
        container.add(otherPortField);
        container.add(connectButton);
    }

    private void initChat(Socket client, Socket server) {
        ChatView chatView = new ChatView(client, server, destPath);
        chatView.setSize(700, 700);
        chatView.setVisible(true);
        chatView.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        chatView.setLocationRelativeTo(this);
    }

    private Socket getClientSocket(int otherPort, ServerThread thread) throws IllegalThreadStateException, InterruptedException {
        Socket clientSocket;
        int time = 5;
        while(true) {
            try {
                clientSocket = getSocket(otherPort);
                break;
            } catch(IOException ex){
                Thread.sleep(1000);
                time -= 1;
                if (time == 0) {
                    // jakas notyfikacja ze timeout
                    JOptionPane.showMessageDialog(this, "Timeout");
                    thread.interrupt();
                }
            }
        }
        return clientSocket;
    }
}
