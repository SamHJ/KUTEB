package com.naijaunik.kuteb.Model;

public class UserModel {

   private String id, phone, is_blocked,first_name,last_name,is_trial,is_paid,current_expiration_date,date_registered,
           email,status,userimage,gender;

    public UserModel() {
    }

    public UserModel(String id, String phone, String is_blocked, String first_name, String last_name,
                     String is_trial, String is_paid, String current_expiration_date,
                     String date_registered, String email, String status, String userimage, String gender) {
        this.id = id;
        this.phone = phone;
        this.is_blocked = is_blocked;
        this.first_name = first_name;
        this.last_name = last_name;
        this.is_trial = is_trial;
        this.is_paid = is_paid;
        this.current_expiration_date = current_expiration_date;
        this.date_registered = date_registered;
        this.email = email;
        this.status = status;
        this.userimage = userimage;
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(String is_blocked) {
        this.is_blocked = is_blocked;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getIs_trial() {
        return is_trial;
    }

    public void setIs_trial(String is_trial) {
        this.is_trial = is_trial;
    }

    public String getIs_paid() {
        return is_paid;
    }

    public void setIs_paid(String is_paid) {
        this.is_paid = is_paid;
    }

    public String getCurrent_expiration_date() {
        return current_expiration_date;
    }

    public void setCurrent_expiration_date(String current_expiration_date) {
        this.current_expiration_date = current_expiration_date;
    }

    public String getDate_registered() {
        return date_registered;
    }

    public void setDate_registered(String date_registered) {
        this.date_registered = date_registered;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
