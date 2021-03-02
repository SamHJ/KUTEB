package com.naijaunik.kuteb.Model;

public class NotifsModel {

    public String id, title, description, date, user_id, is_seen;

    public NotifsModel() {
    }

    public NotifsModel(String id, String title, String description, String date, String user_id, String is_seen) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.user_id = user_id;
        this.is_seen = is_seen;
    }

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getIs_seen() {
        return is_seen;
    }

    public void setIs_seen(String is_seen) {
        this.is_seen = is_seen;
    }
}
