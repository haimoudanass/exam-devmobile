package com.smartcity.app;

public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    public final int type;
    public final String text;
    public final long timestamp;

    public ChatMessage(int type, String text) {
        this.type = type;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }
}
