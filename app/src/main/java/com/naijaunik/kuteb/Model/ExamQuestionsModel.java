package com.naijaunik.kuteb.Model;

public class ExamQuestionsModel {

    private String id, exam_id, subject, question, question_file, question_file_type, question_options,
            correct_answer, question_difficulty;

    public ExamQuestionsModel() {
    }

    public ExamQuestionsModel(String id, String exam_id, String subject, String question, String question_file, String question_file_type,
                              String question_options, String correct_answer, String question_difficulty) {
        this.id = id;
        this.exam_id = exam_id;
        this.subject = subject;
        this.question = question;
        this.question_file = question_file;
        this.question_file_type = question_file_type;
        this.question_options = question_options;
        this.correct_answer = correct_answer;
        this.question_difficulty = question_difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExam_id() {
        return exam_id;
    }

    public void setExam_id(String exam_id) {
        this.exam_id = exam_id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestion_file() {
        return question_file;
    }

    public void setQuestion_file(String question_file) {
        this.question_file = question_file;
    }

    public String getQuestion_file_type() {
        return question_file_type;
    }

    public void setQuestion_file_type(String question_file_type) {
        this.question_file_type = question_file_type;
    }

    public String getQuestion_options() {
        return question_options;
    }

    public void setQuestion_options(String question_options) {
        this.question_options = question_options;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }

    public void setCorrect_answer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    public String getQuestion_difficulty() {
        return question_difficulty;
    }

    public void setQuestion_difficulty(String question_difficulty) {
        this.question_difficulty = question_difficulty;
    }
}
