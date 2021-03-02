package com.naijaunik.kuteb.Model;

public class CoursesModel {

    private String id, title, description, no_of_students_enrolled, status, course_icon;

    public CoursesModel() {
    }

    public CoursesModel(String id, String title, String description,
                        String no_of_students_enrolled, String status, String course_icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.no_of_students_enrolled = no_of_students_enrolled;
        this.status = status;
        this.course_icon = course_icon;
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

    public String getNo_of_students_enrolled() {
        return no_of_students_enrolled;
    }

    public void setNo_of_students_enrolled(String no_of_students_enrolled) {
        this.no_of_students_enrolled = no_of_students_enrolled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCourse_icon() {
        return course_icon;
    }

    public void setCourse_icon(String course_icon) {
        this.course_icon = course_icon;
    }
}
