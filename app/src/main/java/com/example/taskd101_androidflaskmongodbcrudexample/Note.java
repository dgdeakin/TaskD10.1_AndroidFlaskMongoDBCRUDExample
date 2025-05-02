package com.example.taskd101_androidflaskmongodbcrudexample;

public class Note {
    private String _id;
    private String title;
    private String content;
    private String category;

    public Note(String _id, String title, String content, String category) {
        this._id = _id;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public String get_id() { return _id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
}