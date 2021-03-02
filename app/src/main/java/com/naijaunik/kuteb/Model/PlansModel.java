package com.naijaunik.kuteb.Model;

public class PlansModel {

    private String id,title, amount,plan_duration_name,plan_duration_in_days;

    public PlansModel() {
    }

    public PlansModel(String id, String title, String amount,
                      String plan_duration_name, String plan_duration_in_days) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.plan_duration_name = plan_duration_name;
        this.plan_duration_in_days = plan_duration_in_days;
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
