package org.example.ui.threading;

import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;

import java.io.*;
import java.util.Random;

@RequiredArgsConstructor
public class ListenerThread implements Runnable {
    private final ObjectInputStream readStream;
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
                } else if (message instanceof byte[]) {
                    MagicMatch match = Magic.getMagicMatch((byte[]) message);
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
                        os.write((byte[]) message);
                    }
                    board.put(tmp.getName());
                }
            } catch (IOException | ClassNotFoundException | MagicMatchNotFoundException | MagicParseException | MagicException e) {
                e.printStackTrace();
            }
        }
    }
}
