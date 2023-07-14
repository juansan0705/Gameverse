package com.jsancre.gameverse.models;

import java.util.ArrayList;

/**
 * Los modelos los utilizo para crear las tablas en la base de datos de FireStore
 */
public class Comment {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    private String id;
    private String comment;
    private String idUser;
    private String idPost;
    private ArrayList<String> idLike;
    private ArrayList<String> idDislike;
    private int rating;
    private Long timestamp;

    //--------------------
    //     CONSTRUCTOR
    //--------------------
    public Comment() {
    }

    public Comment(String id, String comment, String idUser, String idPost, ArrayList<String> idLike, ArrayList<String> idDislike, int rating, Long timestamp) {
        this.id = id;
        this.comment = comment;
        this.idUser = idUser;
        this.idPost = idPost;
        this.idLike = idLike;
        this.idDislike = idDislike;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    //--------------------
    //     MÉTODOS
    //--------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdPost() {
        return idPost;
    }

    public void setIdPost(String idPost) {
        this.idPost = idPost;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    // incrementa el rating (cuando se da un like)
    public void like() {
        this.rating++;
    }

    // decrementa el rating (cuando se da un dislike)
    public void dislike() {
        this.rating--;
    }

    public ArrayList<String> getIdLike() {
        return idLike;
    }

    public void setIdLike(ArrayList<String> idLike) {
        this.idLike = idLike;
    }

    public ArrayList<String> getIdDislike() {
        return idDislike;
    }

    public void setIdDislike(ArrayList<String> idDislike) {
        this.idDislike = idDislike;
    }
}
