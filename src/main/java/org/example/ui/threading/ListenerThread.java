package org.example.ui.threading;

import lombok.AllArgsConstructor;
import org.example.cryptography.Cryptography;
import org.example.cryptography.GeneratorOfKeys;
import org.example.networking.Message;
import org.example.networking.MessageType;
import org.example.ui.ChatView;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Random;


@AllArgsConstructor
public class ListenerThread implements Runnable {
    private final ObjectInputStream readStream;
    private final ObjectOutputStream writeStream;
    private final MessageBoard board;
    private final String destPath;
    private final JFrame window;
    private final Random random = new Random();
    private ChatView chatView;
    private String extension;
    private KeyPair keyPair;
    private KeyBoard keyBoard;

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Message message = (Message) readStream.readObject();
                if (message != null) {
                    switch (message.getType()) {
                        case CBC_MESSAGE -> {
                            SecretKey key = GeneratorOfKeys.getKeyFromPassword(chatView.getSessionKey(), "2137");
                            String msg = Cryptography.decrypt("AES/CBC/PKCS5Padding", message.getText(), key, new IvParameterSpec(message.getIv()));
                            board.put(msg);
                            chatView.setIv(Arrays.copyOfRange(message.getText().getBytes(), 0, 16));
                        }
                        case ECB_MESSAGE -> {
                            SecretKey key = GeneratorOfKeys.getKeyFromPassword(chatView.getSessionKey(), "2137");
                            String msg = Cryptography.decrypt("AES/ECB/PKCS5Padding", message.getText(), key, null);
                            board.put(msg);
                        }
                        case NOTIFY -> JOptionPane.showMessageDialog(window, "[Other guy]: " + message.getText(), "Notification", JOptionPane.INFORMATION_MESSAGE);
                        case EXTENSION -> extension = message.getText();
                        case CBC_FILE -> {
                            SecretKey key = GeneratorOfKeys.getKeyFromPassword(chatView.getSessionKey(), "2137");
                            byte[] buff;
                            Message obj;
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            while (true) {
                                obj = (Message) readStream.readObject();
                                if (obj == null) {
                                    break;
                                }
                                buff = Cryptography.decryptBytes("AES/CBC/NoPadding", obj.getData(), key, new IvParameterSpec(message.getIv()));
                                outputStream.writeBytes(buff);
                            }
                            byte[] fileOut = outputStream.toByteArray();
                            File tmp = new File(destPath + random.nextInt() + extension);
                            try (OutputStream os = new FileOutputStream(tmp)) {
                                os.write(fileOut);
                            }
                            board.put("Sent you file -> " + tmp.getName());
                            writeStream.writeObject(new Message(MessageType.NOTIFY, "File received"));
                        }
                        case ECB_FILE -> {
                            SecretKey key = GeneratorOfKeys.getKeyFromPassword(chatView.getSessionKey(), "2137");
                            byte[] buff;
                            Message obj;
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            while (true) {
                                obj = (Message) readStream.readObject();
                                if (obj == null) {
                                    break;
                                }
                                buff = Cryptography.decryptBytes("AES/ECB/PKCS5Padding", obj.getData(), key, null);
                                outputStream.writeBytes(buff);
                            }
                            byte[] fileOut = outputStream.toByteArray();
                            File tmp = new File(destPath + random.nextInt() + extension);
                            try (OutputStream os = new FileOutputStream(tmp)) {
                                os.write(fileOut);
                            }
                            board.put("Sent you file -> " + tmp.getName());
                            writeStream.writeObject(new Message(MessageType.NOTIFY, "File received"));
                        }
                        case KEY_REQUEST -> {
                            System.out.println("got key request");
                            var msg = new Message(MessageType.KEY_RESPONSE, keyPair.getPublic());
                            writeStream.writeObject(msg);
                        }
                        case KEY_RESPONSE -> {
                            System.out.println("got key response");
                            keyBoard.put(message.getPk());
                            chatView.setOtherPublicKey(message.getPk());
                        }
                        case SESSION_ID -> {
                            String sessionKey = Cryptography.decryptWithRSA(keyPair.getPrivate(), message.getText());
                            chatView.setSessionKey(sessionKey);
                            System.out.println("SESSION KEY: " + sessionKey);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
