package com.falapp.fortunetelle.models;

public class FortuneTeller {
    private int id;
    private String name;
    private boolean isOnline;
    private float rating;
    private String specialties;
    private int price;
    private String iconResource;


    public FortuneTeller(int id, String name, boolean isOnline, float rating,
                         String specialties, int price, String iconResource) {
        this.id = id;
        this.name = name;
        this.isOnline = isOnline;
        this.rating = rating;
        this.specialties = specialties;
        this.price = price;
        this.iconResource = iconResource;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getIconResource() {
        return iconResource;
    }

    public void setIconResource(String iconResource) {
        this.iconResource = iconResource;
    }


    public String[] getSpecialtiesArray() {
        return specialties.split(",");
    }


    public boolean hasSpecialty(String specialty) {
        return specialties.contains(specialty);
    }
}