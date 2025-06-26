package com.falapp.fortunetelle.models;

public class User {
    private int id;
    private String name;
    private int coins;
    private boolean isPremium;


    public User(int id, String name, int coins, boolean isPremium) {
        this.id = id;
        this.name = name;
        this.coins = coins;
        this.isPremium = isPremium;
    }


    public User(String name, int coins) {
        this.name = name;
        this.coins = coins;
        this.isPremium = false;
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

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }


    public void addCoins(int amount) {
        this.coins += amount;
    }

    // Method to remove coins from user's balance
    public boolean removeCoins(int amount) {
        if (this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }


    public boolean hasEnoughCoins(int amount) {
        return this.coins >= amount;
    }
}