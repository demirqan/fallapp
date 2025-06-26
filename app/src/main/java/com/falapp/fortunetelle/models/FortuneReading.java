package com.falapp.fortunetelle.models;

public class FortuneReading {
    private int id;
    private String type; // "coffee", "tarot", "palm", "face"
    private int fortuneTellerId;
    private int userId;
    private String topics; // Comma-separated list: "aşk,para,sağlık"
    private String userName;
    private String birthDate;
    private String relationshipStatus;
    private String jobStatus;
    private String photoPath;
    private String result;
    private long timestamp;
    private long availableTime;
    private boolean isAvailable;


    public FortuneReading(int id, String type, int fortuneTellerId, int userId,
                          String topics, String userName, String birthDate,
                          String relationshipStatus, String jobStatus, String photoPath,
                          String result, long timestamp, long availableTime, boolean isAvailable) {
        this.id = id;
        this.type = type;
        this.fortuneTellerId = fortuneTellerId;
        this.userId = userId;
        this.topics = topics;
        this.userName = userName;
        this.birthDate = birthDate;
        this.relationshipStatus = relationshipStatus;
        this.jobStatus = jobStatus;
        this.photoPath = photoPath;
        this.result = result;
        this.timestamp = timestamp;
        this.availableTime = availableTime;
        this.isAvailable = isAvailable;
    }


    public FortuneReading(String type, int fortuneTellerId, int userId,
                          String topics, String userName, String birthDate,
                          String relationshipStatus, String jobStatus, String photoPath) {
        this.type = type;
        this.fortuneTellerId = fortuneTellerId;
        this.userId = userId;
        this.topics = topics;
        this.userName = userName;
        this.birthDate = birthDate;
        this.relationshipStatus = relationshipStatus;
        this.jobStatus = jobStatus;
        this.photoPath = photoPath;
        this.result = "";
        this.timestamp = System.currentTimeMillis();

        this.availableTime = this.timestamp + (3 * 60 * 1000) + (int)(Math.random() * (2 * 60 * 1000));
        this.isAvailable = false;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFortuneTellerId() {
        return fortuneTellerId;
    }

    public void setFortuneTellerId(int fortuneTellerId) {
        this.fortuneTellerId = fortuneTellerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(long availableTime) {
        this.availableTime = availableTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


    public String[] getTopicsArray() {
        return topics.split(",");
    }


    public boolean isReadyToView() {
        return isAvailable || System.currentTimeMillis() >= availableTime;
    }


    public long getRemainingTime() {
        if (isAvailable) {
            return 0;
        }
        long remaining = availableTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }


    public String getReadableType() {
        switch (type) {
            case "coffee":
                return "Kahve Falı";
            case "tarot":
                return "Tarot Falı";
            case "palm":
                return "El Falı";
            case "face":
                return "Yüz Falı";
            default:
                return type;
        }
    }
}