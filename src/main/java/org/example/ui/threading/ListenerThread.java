package org.example.ui.threading;

import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
public class ListenerThread implements Runnable {
    private final ObjectInputStream readStream;
    private final ObjectOutputStream writeStream;
    private final MessageBoard board;
    private final String destPath;
    private final JFrame window;
    private final Random random = new Random();
    private String extension = "";

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Object message = readStream.readObject();
                if (message instanceof String) {
                    String tmp = (String) message;
                    board.put(tmp);
                } else if (message instanceof char[]) {
                    String tmp = new String((char[]) message);
                    JOptionPane.showMessageDialog(window, "[Other guy]: " + tmp, "Notification", JOptionPane.INFORMATION_MESSAGE);
                }
                else if(message instanceof Map){
                    Map<String, String> map = (Map<String, String>) message;
                    extension = map.get("ext");
                }

                else if (message instanceof byte[]) {
                    //czesc pliku
                    byte[] buff = new byte[1024];
                    int i = 1;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    while (i != -1) {
                        i = readStream.read(buff);
                        outputStream.writeBytes(buff);
                    }
                    byte[] fileOut = outputStream.toByteArray();
                    //String extension = decideExtension(fileOut);
                    File tmp = new File(destPath + random.nextInt() + extension);
                    try (OutputStream os = new FileOutputStream(tmp)) {
                        os.write(fileOut);
                    }
                    board.put("Sent you file -> " + tmp.getName());
                    writeStream.writeObject("file received".toCharArray());
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
