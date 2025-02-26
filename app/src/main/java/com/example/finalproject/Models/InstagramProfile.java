package com.example.finalproject.Models;

public class InstagramProfile {
    private int imageResId;
    private String instagramUrl;
    public InstagramProfile(int imageResId, String instagramUrl) {
        this.imageResId = imageResId;
        this.instagramUrl = instagramUrl;
    }
    public int getImageResId() {
        return imageResId;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }
}
