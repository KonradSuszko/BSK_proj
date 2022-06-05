package org.example.ui;

import lombok.Getter;
import lombok.Setter;
import org.example.cryptography.Cryptography;
import org.example.cryptography.GeneratorOfKeys;
import org.example.cryptography.RSAUtils;
import org.example.networking.Message;
import org.example.networking.MessageType;
import org.example.ui.threading.AppenderThread;
import org.example.ui.threading.KeyBoard;
import org.example.ui.threading.ListenerThread;
import org.example.ui.threading.MessageBoard;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

@Getter
public class ChatView extends JFrame implements ActionListener {
    final Container container = getContentPane();
    final JScrollPane scrollPane = new JScrollPane();
    final JTextArea chatArea = new JTextArea();
    final JTextField field = new JTextField();
    final JButton sendButton = new JButton("SEND");
    final JButton fileButton = new JButton("FILE");
    private final KeyPair keys;
    private final transient KeyBoard keyBoard = new KeyBoard();
    private transient ObjectOutputStream writeStream;
    private transient ObjectInputStream readStream;
    private byte[] iv;
    @Setter
    private String sessionKey = null;
    @Setter
    private PublicKey otherPublicKey = null;
    private String password;
    private String privateKeyPath;
    private String publicKeyPath;

    public ChatView(@NotNull Socket clientSocket, @NotNull Socket serverSocket, String destPath, String privateKeyPath, String publicKeyPath, String password) throws HeadlessException {
        setTitle("Chat");
        container.setLayout(null);
        setLocationAndSize();
        addComponentsToContainer();
        chatArea.setEditable(false);
        sendButton.addActionListener(this);
        field.addActionListener(this);
        fileButton.addActionListener(this);
        iv = GeneratorOfKeys.generateIv();
        this.password = password;
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
        try {
            this.keys = RSAUtils.initialize(privateKeyPath, publicKeyPath, password);
        } catch (Exception ex){
            System.err.println("Wrong password");
            throw new RuntimeException("Wrong password");
        }
        try {
            writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
            readStream = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        MessageBoard board = new MessageBoard();
        Thread listener = new Thread(new ListenerThread(
                readStream,
                writeStream,
                board,
                destPath,
                this,
                this,
                "",
                keys,
                keyBoard
        ));
        Thread appender = new Thread(new AppenderThread(board, chatArea));
        listener.start();
        appender.start();
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource() == sendButton || e.getSource() == field) {
            try {
                assert writeStream != null;
                writeStream.flush();
                String message = field.getText();
                chatArea.append("\n");
                chatArea.append("[me]: " + message);
                field.setText("");
                ensureSessionKey();
                SecretKey key = GeneratorOfKeys.getKeyFromPassword(sessionKey, "2137");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                message = Cryptography.encrypt("AES/CBC/PKCS5Padding", message, key, ivSpec);
                Message msg = new Message(MessageType.CBC_MESSAGE, message, iv);
                writeStream.writeObject(msg);
            } catch (Exception ex) {
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
                extension = file.toString().substring(dotIndex);
            } else {
                return;
            }
            try {
                ensureSessionKey();
                sendFileWithProgressBar(file, extension);
            } catch (Exception ex) {
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

    private void sendFileWithProgressBar(@NotNull File file, String extension) {
        try (InputStream in = new FileInputStream(file.getPath())) {
            writeStream.flush();
            writeStream.writeObject(new Message(MessageType.EXTENSION, extension));
            ProgressBar pb = new ProgressBar((int) file.length());
            pb.setVisible(true);
            int val = 0;
            int c;
            byte[] bytes = new byte[1024];

            SecretKey key = GeneratorOfKeys.getKeyFromPassword(sessionKey, "2137");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            byte[] ciphered;
            writeStream.writeObject(new Message(MessageType.CBC_FILE, bytes, iv)); // sample
            while ((c = in.read(bytes)) != -1) {

                //writeStream.write(bytes, 0, 1024);
                //
                // ciphered = Cryptography.encryptBytes("AES/CBC/PKCS5Padding", Arrays.copyOfRange(bytes, 0, 1024), key, ivSpec);

                ciphered = Cryptography.encryptBytes("AES/CBC/NoPadding", Arrays.copyOfRange(bytes, 0, 1024), key, ivSpec); //wysypuje sie bo byl padding
                System.out.println(new String(ciphered));
                writeStream.writeObject(new Message(MessageType.CBC_FILE, ciphered, iv));
                val += c;
                pb.getJb().setValue(val);
                pb.update(pb.getGraphics());
            }
            writeStream.writeObject(null);
            chatArea.append("\n[me]: File sent -> " + file.getName());
            pb.setVisible(false);
            pb.dispose();
        } catch (IOException | InvalidKeySpecException | NoSuchPaddingException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
    }

    public void ensureSessionKey() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException {
        if (sessionKey == null) {
            Message message1 = new Message(MessageType.KEY_REQUEST);
            writeStream.writeObject(message1);
            System.out.println("Waiting for response...");
            otherPublicKey = keyBoard.take();
            sessionKey = UUID.randomUUID().toString();
            String encryptedSessionKey = Cryptography.encryptWithRSA(otherPublicKey, sessionKey);
            Message message2 = new Message(MessageType.SESSION_ID, encryptedSessionKey);
            writeStream.writeObject(message2);
            System.out.printf("Session key: %s%n", sessionKey);
        }
    }
}
