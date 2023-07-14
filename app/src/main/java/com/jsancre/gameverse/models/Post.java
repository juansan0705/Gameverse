package com.jsancre.gameverse.models;

/**
 * Los modelos los utilizo para crear las tablas en la base de datos de FireStore
 */
public class Post {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    private String id;
    private String title;
    private String description;
    private String image1;
    private String image2;
    private String idUser;
    private String category;
    private Long timestamp;

    //--------------------
    //     CONSTRUCTOR
    //--------------------
    public Post() {
    }

    public Post(String id, String title, String description, String image1, String image2, String idUser, String category, Long timastamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image1 = image1;
        this.image2 = image2;
        this.idUser = idUser;
        this.category = category;
        this.timestamp = timastamp;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
