package com.falapp.fortunetelle.models;

public class JournalEntry {
    private int id;
    private int userId;
    private long date;
    private String content;
    private long lastModified;


    public JournalEntry(int id, int userId, long date, String content, long lastModified) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.content = content;
        this.lastModified = lastModified;
    }


    public JournalEntry(int userId, long date, String content, long lastModified) {
        this.userId = userId;
        this.date = date;
        this.content = content;
        this.lastModified = lastModified;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }


    public String getFormattedDate() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());
        return dateFormat.format(new java.util.Date(date));
    }


    public String getFormattedLastModified() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return dateFormat.format(new java.util.Date(lastModified));
    }
}