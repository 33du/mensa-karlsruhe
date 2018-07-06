package com.oops.mensa.model;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private String meal;
    private String dish;
    private List<String> additives;
    private List<Double> prices;

    public Meal() {
        this.additives = new ArrayList<>();
        this.prices = new ArrayList<>();
    }

    public List<Double> getPrices() {
        return prices;
    }

    public void setPrices(List<Double> prices) {
        this.prices = prices;
    }

    public List<String> getAdditives() {
        return additives;
    }

    public void setAdditives(List<String> additives) {
        this.additives = additives;
    }

    public String getDish() {
        return dish;
    }

    public void setDish(String dish) {
        this.dish = dish;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }
}
