package org.example.networking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Message implements Serializable {
    // 1 - wiadomosc CBC
    // 2 - powiadomienie
    // 3 - rozszerzenie
    private int type;
    private String text;
    private byte[] data;
    private byte[] iv;

    public Message(int type, String text, byte[] iv){
        this.type = type;
        this.text = text;
        this.iv = iv;
    }

    public Message(int type, String text){
        this.type = type;
        this.text = text;
    }

    public Message(int type, byte[] data, byte[] iv){
        this.type = type;
        this.data = data;
        this.iv = iv;
    }
}
