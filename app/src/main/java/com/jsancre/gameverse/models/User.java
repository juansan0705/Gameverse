package com.jsancre.gameverse.models;

/**
 * Los modelos los utilizo para crear las tablas en la base de datos de FireStore
 */
public class User {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    private String id;
    private String email;
    private String username;
    private String phone;
    private String image_profile;
    private String image_cover;
    private long timestamp;
    private boolean online;
    private boolean adminPrivilege;
    private long lastConnection;


    //--------------------
    //   CONSTRUCTORES
    //--------------------
    public User(){

    }

    public User(String id, String email, String username, String phone, String image_profile, String image_cover, long timestamp, boolean online, boolean adminPrivilege, long lastConnection) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.image_profile = image_profile;
        this.image_cover = image_cover;
        this.timestamp = timestamp;
        this.online = online;
        this.adminPrivilege = adminPrivilege;
        this.lastConnection = lastConnection;
    }

    //--------------------
    //  GETTERS Y SETTERS
    //--------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_profile() {
        return image_profile;
    }

    public void setImage_profile(String image_profile) {
        this.image_profile = image_profile;
    }

    public String getImage_cover() {
        return image_cover;
    }

    public void setImage_cover(String image_cover) {
        this.image_cover = image_cover;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(long lastConnection) {
        this.lastConnection = lastConnection;
    }

    public boolean isAdmin() {
        return adminPrivilege;
    }

    public void setAdminPrivilege(boolean adminPrivilege) {
        this.adminPrivilege = adminPrivilege;
    }
}
