package com.naijaunik.kuteb.Model;

public class HomeSlider {
    private int id;
    private String image, title,subtitle,go_to_url;

    public HomeSlider() {
    }

    public HomeSlider(int id, String image, String title,String subtitle, String go_to_url) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.subtitle = subtitle;
        this.go_to_url = go_to_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getGo_to_url() {
        return go_to_url;
    }

    public void setGo_to_url(String go_to_url) {
        this.go_to_url = go_to_url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
