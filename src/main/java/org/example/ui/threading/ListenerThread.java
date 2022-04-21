package org.example.ui.threading;

import lombok.AllArgsConstructor;
import net.sf.jmimemagic.*;

import java.io.*;
import java.util.Random;

@AllArgsConstructor
public class ListenerThread implements Runnable {
    private ObjectInputStream readStream;
    private MessageBoard board;
    private String destPath;

    @Override
    public void run() {
        while (true) {
            try {
                var message = readStream.readObject();
                if (message instanceof String) {
                    String tmp = (String) message;
                    board.put(tmp);
                } else if (message instanceof byte[]) {
                    Magic parser = new Magic();
                    MagicMatch match = parser.getMagicMatch((byte[]) message);
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
                    Random random = new Random();
                    File tmp = new File(destPath + "/" + random.nextInt() + extension);
                    OutputStream os = new FileOutputStream(tmp);
                    os.write((byte[]) message);
                    os.close();
                    board.put(tmp.getName());
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (MagicMatchNotFoundException e) {
                e.printStackTrace();
            } catch (MagicException e) {
                e.printStackTrace();
            } catch (MagicParseException e) {
                e.printStackTrace();
            }
        }
    }
}
