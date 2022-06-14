package org.example.networking;

import lombok.Getter;

import java.io.Serializable;
import java.security.PublicKey;

@Getter
public class Message implements Serializable {
    private final MessageType type;
    private String text;
    private byte[] data;
    private byte[] iv;
    private PublicKey pk;

    public Message(MessageType type, String text, byte[] iv) {
        this.type = type;
        this.text = text;
        this.iv = iv;
    }

    public Message(MessageType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Message(MessageType type, byte[] data, byte[] iv) {
        this.type = type;
        this.data = data;
        this.iv = iv;
    }

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, PublicKey key) {
        this.type = type;
        pk = key;
    }
}
