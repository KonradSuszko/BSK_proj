package org.example.ui;

import org.example.ui.threading.AppenderThread;
import org.example.ui.threading.ListenerThread;
import org.example.ui.threading.MessageBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatView extends JFrame implements ActionListener {
    final Container container = getContentPane();
    final JScrollPane scrollPane = new JScrollPane();
    final JTextArea chatArea = new JTextArea();
    final JTextField field = new JTextField();
    final JButton sendButton = new JButton("SEND");
    final JButton fileButton = new JButton("FILE");
    private transient ObjectOutputStream writeStream;
    private transient ObjectInputStream readStream;

    public ChatView(Socket clientSocket, @NotNull Socket serverSocket, String destPath) throws HeadlessException {
        setTitle("Chat");
        container.setLayout(null);
        setLocationAndSize();
        addComponentsToContainer();
        chatArea.setEditable(false);
        sendButton.addActionListener(this);
        field.addActionListener(this);
        fileButton.addActionListener(this);

        try {
            writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
            readStream = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        MessageBoard board = new MessageBoard();
        Thread listener = new Thread(new ListenerThread(readStream, writeStream, board, destPath, this));
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == fileButton) {
            JFileChooser jf = new JFileChooser();
            int returnVal = jf.showDialog(this, "Upload");
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = jf.getSelectedFile();
            int dotIndex = file.toString().lastIndexOf('.');
            String extension;
            if (dotIndex > 0) {
                extension = file.toString().substring(dotIndex + 1);
            } else {
                return;
            }
            if (!extension.equalsIgnoreCase("txt") && !extension.equalsIgnoreCase("pdf")
                    && !extension.equalsIgnoreCase("png") && !extension.equalsIgnoreCase("avi")) {
                JOptionPane.showMessageDialog(this, "Bad format");
                return;
            }
            sendFileWithProgressBar(file);
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

    private void sendFileWithProgressBar(File file){
        try (InputStream in = new FileInputStream(file.getPath())) {
            writeStream.flush();
            ProgressBar pb = new ProgressBar((int) file.length());
            pb.setVisible(true);

            int val = 0;
            int c;
            byte[] bytes = new byte[1024];
            writeStream.writeObject(bytes); // sample
            while ((c = in.read(bytes)) != -1) {
                writeStream.write(bytes, 0, 1024);
                val += c;
                pb.getJb().setValue(val);
                pb.update(pb.getGraphics());
            }
            writeStream.writeObject(null);
            chatArea.append("\n[me]: File sent -> " + file.getName());
            pb.setVisible(false);
            pb.dispose();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
