package org.example.ui.threading;

import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;

import javax.swing.*;
import java.io.*;
import java.util.Random;

@RequiredArgsConstructor
public class ListenerThread implements Runnable {
    private final ObjectInputStream readStream;
    private final ObjectOutputStream writeStream;
    private final MessageBoard board;
    private final String destPath;
    private final Random random = new Random();

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                var message = readStream.readObject();
                if (message instanceof String) {
                    String tmp = (String) message;
                    board.put(tmp);
                    writeStream.writeObject(("Received message -> " + message).toCharArray());

                } else if(message instanceof char[]) {
                    String tmp = new String((char[])message);
                    board.put(tmp);
                } else if (message instanceof byte[]) {
                    //czesc pliku
                    try {
                        byte[] buff = new byte[1024];
                        int i = 1;
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        while (i != -1) {
                            i = readStream.read(buff);
                            outputStream.writeBytes(buff);
                        }
                        byte[] fileOut = outputStream.toByteArray();
                        MagicMatch match = Magic.getMagicMatch(fileOut);
                        String extension = "";
                        String mime = match.getMimeType();
                        if (mime.equalsIgnoreCase("text/plain")) {
                            extension = ".txt";
                        } else if (mime.equalsIgnoreCase("application/pdf")) {
                            extension = ".pdf";
                        } else if (mime.equalsIgnoreCase("image/png")) {
                            extension = ".png";
                        } else if (mime.equalsIgnoreCase("???")) {
                            extension = ".avi";
                        }
                        File tmp = new File(destPath + random.nextInt() + extension);
                        try (OutputStream os = new FileOutputStream(tmp)) {

                            os.write(fileOut);
                        }
                        board.put("Sent you file -> " + tmp.getName());
                    } catch (IOException | MagicMatchNotFoundException | MagicParseException | MagicException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException | ClassNotFoundException  e) {
                e.printStackTrace();
            }
        }
    }
}
