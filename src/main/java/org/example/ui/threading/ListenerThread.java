package org.example.ui.threading;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.example.ui.ChatView;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
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
    private String extension = "";

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Message message = (Message)readStream.readObject();
                if (message != null) {
                    SecretKey key = GeneratorOfKeys.getKeyFromPassword("secret", "2137");
                    switch (message.getType()) {
                        case 1:
                            System.out.println(message.getIv().toString());
                            String msg = Cryptography.decrypt("AES/CBC/PKCS5Padding", message.getText(), key, new IvParameterSpec(message.getIv()));
                            board.put(msg);
                            chatView.setIv(Arrays.copyOfRange(message.getText().getBytes(), 0, 16));
                            break;
                        case 2:
                            JOptionPane.showMessageDialog(window, "[Other guy]: " + message.getText(), "Notification", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        case 3:
                            extension = message.getText();
                            break;
                        case 4:
                            byte[] buff = new byte[1024];
                            int i = 1;
                            Message obj;
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            System.out.println("received");
                            while (i != -1) {
                                //i = readStream.read(buff);
                                obj = (Message)readStream.readObject();
                                if (obj == null)
                                    break;
                                System.out.println(new String(obj.getData()));
                                buff = Cryptography.decryptBytes("AES/CBC/NoPadding", obj.getData(), key, new IvParameterSpec(message.getIv()));
                                //chatView.setIv(Arrays.copyOfRange(message.getText().getBytes(), 0, 16));
                                //outputStream.writeBytes(buff);

                                //outputStream.writeBytes(obj.getData());
                                outputStream.writeBytes(buff);
                            }
                            byte[] fileOut = outputStream.toByteArray();
                            System.out.println();
                            System.out.println(new String(fileOut));
                            //String extension = decideExtension(fileOut);
                            File tmp = new File(destPath + random.nextInt() + extension);
                            try (OutputStream os = new FileOutputStream(tmp)) {
                                os.write(fileOut);
                            }
                            board.put("Sent you file -> " + tmp.getName());
                            writeStream.writeObject(new Message(2, "File received"));
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private @NotNull String decideExtension(byte[] file) throws MagicMatchNotFoundException, MagicException, MagicParseException {
        MagicMatch match = Magic.getMagicMatch(file);
        String mime = match.getMimeType();
        if (mime.equalsIgnoreCase("text/plain")) {
            return ".txt";
        } else if (mime.equalsIgnoreCase("application/pdf")) {
            return ".pdf";
        } else if (mime.equalsIgnoreCase("image/png")) {
            return ".png";
        } else if (mime.equalsIgnoreCase("???")) {
            return ".avi";
        }
        return "";
    }
}
