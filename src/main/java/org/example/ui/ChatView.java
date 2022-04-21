package org.example.ui;

import org.example.ui.threading.AppenderThread;
import org.example.ui.threading.ListenerThread;
import org.example.ui.threading.MessageBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatView extends JFrame implements ActionListener {
    private final Socket clientSocket;
    final Container container = getContentPane();
    final JScrollPane scrollPane = new JScrollPane();
    final JTextArea chatArea = new JTextArea();
    final JTextField field = new JTextField();
    final JButton sendButton = new JButton("SEND");
    final JButton fileButton = new JButton("FILE");


    public ChatView(Socket clientSocket, Socket serverSocket) throws HeadlessException {
        this.clientSocket = clientSocket;
        container.setLayout(null);
        setLocationAndSize();
        addComponentsToContainer();
        chatArea.setEditable(false);
        sendButton.addActionListener(this);
        field.addActionListener(this);

        MessageBoard board = new MessageBoard();
        DataInputStream readStream = null;
        try {
            readStream = new DataInputStream(serverSocket.getInputStream());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        Thread listener = new Thread(new ListenerThread(readStream, board));
        Thread appender = new Thread(new AppenderThread(board, chatArea));
        listener.start();
        appender.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataOutputStream writeStream = null;
        try {
            writeStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        if (e.getSource() == sendButton || e.getSource() == field) {
            try {
                assert writeStream != null;
                writeStream.flush();
                String message = field.getText();
                chatArea.append("\n");
                chatArea.append("[me]: " + message);
                field.setText("");
                writeStream.writeUTF(message);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private void setLocationAndSize() {
        field.setBounds(0, 525, 525, 175);
        fileButton.setBounds(525, 525, 75, 175);
        sendButton.setBounds(600, 525, 100, 175);
        scrollPane.setBounds(0, 0, 700, 525);

        chatArea.setColumns(20);
        chatArea.setRows(5);
        scrollPane.setViewportView(chatArea);
    }

    private void addComponentsToContainer() {
        container.add(scrollPane);
        container.add(field);
        container.add(sendButton);
        container.add(fileButton);
    }
}
