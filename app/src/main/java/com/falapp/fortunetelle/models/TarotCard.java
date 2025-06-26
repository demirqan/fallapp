package com.falapp.fortunetelle.models;

public class TarotCard {
    private String name;
    private String meaning;
    private int imageResourceId;
    private boolean isReversed;

    public TarotCard(String name, String meaning, int imageResourceId, boolean isReversed) {
        this.name = name;
        this.meaning = meaning;
        this.imageResourceId = imageResourceId;
        this.isReversed = isReversed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }


    public String getReversedMeaning() {

        return "Ters: " + meaning + " (engellenmiş veya olumsuz etkilenmiş)";
    }


    public String getAppropriateMeaning() {
        return isReversed ? getReversedMeaning() : meaning;
    }
}