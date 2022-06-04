package org.example.networking;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Message implements Serializable {
    // 1 - wiadomosc CBC
    // 2 - powiadomienie
    // 3 - rozszerzenie
    private final MessageType type;
    private String text;
    private byte[] data;
    private byte[] iv;

    public Message(MessageType type, String text, byte[] iv){
        this.type = type;
        this.text = text;
        this.iv = iv;
    }

    public Message(MessageType type, String text){
        this.type = type;
        this.text = text;
    }

    public Message(MessageType type, byte[] data, byte[] iv){
        this.type = type;
        this.data = data;
        this.iv = iv;
    }
}
