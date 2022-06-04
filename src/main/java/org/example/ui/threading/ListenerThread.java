package org.example.ui.threading;

import lombok.AllArgsConstructor;
import org.example.networking.MessageType;
import org.example.ui.ChatView;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import org.example.networking.Message;
import org.example.cryptography.*;


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

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Message message = (Message)readStream.readObject();
                if (message != null) {
                    SecretKey key = GeneratorOfKeys.getKeyFromPassword("secret", "2137");
                    switch (message.getType()) {
                        case CBC_MESSAGE -> {
                            String msg = Cryptography.decrypt("AES/CBC/PKCS5Padding", message.getText(), key, new IvParameterSpec(message.getIv()));
                            board.put(msg);
                            chatView.setIv(Arrays.copyOfRange(message.getText().getBytes(), 0, 16));
                        }
                        case NOTIFY -> JOptionPane.showMessageDialog(window, "[Other guy]: " + message.getText(), "Notification", JOptionPane.INFORMATION_MESSAGE);
                        case EXTENSION -> extension = message.getText();
                        case CBC_FILE -> {
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
                        case SESSION_KEY_NEGOTIATION -> {

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
