package com.jsancre.gameverse.models;

import java.util.ArrayList;

public class Chat {

    //------------------
    //    ATRIBUTOS
    //------------------
    private String idUser1;
    private String id;
    private String idUser2;
    private int idNotification;
    private boolean isWritting;
    private long timestamp;
    private ArrayList<String> ids;

    //------------------
    //   CONSTRUCTOR
    //------------------
    public Chat() {
    }

    public Chat(String idUser1, String id, String idUser2, int idNotification, boolean isWritting, long timestamp, ArrayList<String> ids) {
        this.idUser1 = idUser1;
        this.id = id;
        this.idUser2 = idUser2;
        this.idNotification = idNotification;
        this.isWritting = isWritting;
        this.timestamp = timestamp;
        this.ids = ids;
    }

    //------------------
    // GETTER & SETTERS
    //------------------
    public boolean isWritting() {
        return isWritting;
    }

    public void setWritting(boolean writting) {
        isWritting = writting;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdUser1() {
        return idUser1;
    }

    public void setIdUser1(String idUser1) {
        this.idUser1 = idUser1;
    }

    public String getIdUser2() {
        return idUser2;
    }

    public void setIdUser2(String idUser2) {
        this.idUser2 = idUser2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }
}
