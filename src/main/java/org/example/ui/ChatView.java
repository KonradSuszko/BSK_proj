package org.example.ui;

import org.example.ui.threading.AppenderThread;
import org.example.ui.threading.ListenerThread;
import org.example.ui.threading.MessageBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;

public class ChatView extends JFrame implements ActionListener {
    private final transient Socket clientSocket;
    private final transient Socket serverSocket;
    final Container container = getContentPane();
    final JScrollPane scrollPane = new JScrollPane();
    final JTextArea chatArea = new JTextArea();
    final JTextField field = new JTextField();
    final JButton sendButton = new JButton("SEND");
    final JButton fileButton = new JButton("FILE");
    private ObjectOutputStream writeStream;
    private ObjectInputStream readStream;
    private String destPath;


    public ChatView(Socket clientSocket, @NotNull Socket serverSocket, String destPath) throws HeadlessException {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        container.setLayout(null);
        setLocationAndSize();
        addComponentsToContainer();
        chatArea.setEditable(false);
        sendButton.addActionListener(this);
        field.addActionListener(this);
        fileButton.addActionListener(this);
        this.destPath = destPath;

        MessageBoard board = new MessageBoard();
        try {
            writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
            readStream = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        Thread listener = new Thread(new ListenerThread(readStream, board, destPath));
        Thread appender = new Thread(new AppenderThread(board, chatArea));
        listener.start();
        appender.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton || e.getSource() == field) {
            try {
                assert writeStream != null;
                writeStream.flush();
                String message = field.getText();
                chatArea.append("\n");
                chatArea.append("[me]: " + message);
                field.setText("");
                writeStream.writeObject(message);
                System.out.println("Sent: " + message);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
        else if(e.getSource() == fileButton){
            JFileChooser jf = new JFileChooser();
            int returnVal = jf.showDialog(this, "Upload");
            if(returnVal != JFileChooser.APPROVE_OPTION){
                return;
            }
            File file = jf.getSelectedFile();
            int dotIndex = file.toString().lastIndexOf('.');
            String extension = null;
            if(dotIndex > 0){
                extension = file.toString().substring(dotIndex + 1);
            }
            else{
                return;
            }

            if(!extension.toLowerCase().equals("txt") && !extension.toLowerCase().equals("pdf")
                    && !extension.toLowerCase().equals("png") && !extension.toLowerCase().equals("avi")){
                JOptionPane.showMessageDialog(this, "Bad format");
                return;
            }
            try {
                writeStream.flush();
                //writeStream.writeObject(file);
                writeStream.writeObject(Files.readAllBytes(file.toPath()));
                chatArea.append("[me]: File sent -> " + file.getName() + "\n");
            } catch (IOException ex){
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
