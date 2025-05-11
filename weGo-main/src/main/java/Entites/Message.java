package Entites;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String content;
    private int senderId;
    private int conversationId;
    private LocalDateTime timestamp;
    private boolean isRead;
    private String senderName; // Pour faciliter l'affichage

    public Message() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public Message(String content, int senderId, int conversationId, String senderName) {
        this();
        this.content = content;
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.senderName = senderName;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
} 