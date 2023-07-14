package com.jsancre.gameverse.models;

/**
 * Los modelos los utilizo para crear las tablas en la base de datos de FireStore
 */
public class SliderItem {
    //---------------------
    //     ATRIBUTOS
    //---------------------
    String imageUrl;
    Long timestamp;

    //---------------------
    //     CONSTRUCTOR
    //---------------------

    public SliderItem() {
    }

    public SliderItem(String imageUrl, Long timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    //---------------------
    //     MÉTODOS
    //---------------------


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
