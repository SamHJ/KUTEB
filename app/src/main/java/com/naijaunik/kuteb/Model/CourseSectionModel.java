package com.naijaunik.kuteb.Model;

public class CourseSectionModel {

    private String id,course_id,section_title,content,video_url,video_from,external_link,attachment,attachment_extension;

    public CourseSectionModel() {
    }

    public CourseSectionModel(String id, String course_id, String section_title,
                              String content, String video_url, String video_from,
                              String external_link, String attachment, String attachment_extension) {
        this.id = id;
        this.course_id = course_id;
        this.section_title = section_title;
        this.content = content;
        this.video_url = video_url;
        this.video_from = video_from;
        this.external_link = external_link;
        this.attachment = attachment;
        this.attachment_extension = attachment_extension;
    }

    public String getAttachment_extension() {
        return attachment_extension;
    }

    public void setAttachment_extension(String attachment_extension) {
        this.attachment_extension = attachment_extension;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_from() {
        return video_from;
    }

    public void setVideo_from(String video_from) {
        this.video_from = video_from;
    }

    public String getExternal_link() {
        return external_link;
    }

    public void setExternal_link(String external_link) {
        this.external_link = external_link;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSection_title() {
        return section_title;
    }

    public void setSection_title(String section_title) {
        this.section_title = section_title;
    }
}
