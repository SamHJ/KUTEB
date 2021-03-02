package com.naijaunik.kuteb.Model;

public class ExamsModel {

    private String id, exam_title, no_of_questions, is_subject_wise;

    public ExamsModel() {
    }

    public ExamsModel(String id, String exam_title, String no_of_questions, String is_subject_wise) {
        this.id = id;
        this.exam_title = exam_title;
        this.no_of_questions = no_of_questions;
        this.is_subject_wise = is_subject_wise;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExam_title() {
        return exam_title;
    }

    public void setExam_title(String exam_title) {
        this.exam_title = exam_title;
    }

    public String getNo_of_questions() {
        return no_of_questions;
    }

    public void setNo_of_questions(String no_of_questions) {
        this.no_of_questions = no_of_questions;
    }

    public String getIs_subject_wise() {
        return is_subject_wise;
    }

    public void setIs_subject_wise(String is_subject_wise) {
        this.is_subject_wise = is_subject_wise;
    }
}
