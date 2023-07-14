package com.jsancre.gameverse.models;

public class Message {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    private String id;
    private String idSender;
    private String idReceiver;
    private String idChat;
    private String message;
    private Long timestamp;
    private boolean viewed;

    public Message() {
    }

    //--------------------
    //     CONSTRUCTOR
    //--------------------
    public Message(String id, String idSender, String idReceiver, String idChat, String message, Long timestamp, boolean viewed) {
        this.id = id;
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.idChat = idChat;
        this.message = message;
        this.timestamp = timestamp;
        this.viewed = viewed;
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
