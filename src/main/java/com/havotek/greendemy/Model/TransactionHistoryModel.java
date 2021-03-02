package com.naijaunik.kuteb.Model;

public class TransactionHistoryModel {

    String id, plan_name, plan_amount, plan_duration_name, plan_duration_in_days, date_paid,
            user_name, user_phone;

    public TransactionHistoryModel() {
    }

    public TransactionHistoryModel(String id, String plan_name, String plan_amount, String plan_duration_name,
                                   String plan_duration_in_days, String date_paid,
                                   String user_name, String user_phone) {
        this.id = id;
        this.plan_name = plan_name;
        this.plan_amount = plan_amount;
        this.plan_duration_name = plan_duration_name;
        this.plan_duration_in_days = plan_duration_in_days;
        this.date_paid = date_paid;
        this.user_name = user_name;
        this.user_phone = user_phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlan_name() {
        return plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    public String getPlan_amount() {
        return plan_amount;
    }

    public void setPlan_amount(String plan_amount) {
        this.plan_amount = plan_amount;
    }

    public String getPlan_duration_name() {
        return plan_duration_name;
    }

    public void setPlan_duration_name(String plan_duration_name) {
        this.plan_duration_name = plan_duration_name;
    }

    public String getPlan_duration_in_days() {
        return plan_duration_in_days;
    }

    public void setPlan_duration_in_days(String plan_duration_in_days) {
        this.plan_duration_in_days = plan_duration_in_days;
    }

    public String getDate_paid() {
        return date_paid;
    }

    public void setDate_paid(String date_paid) {
        this.date_paid = date_paid;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }
}