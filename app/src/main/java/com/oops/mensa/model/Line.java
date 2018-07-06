package com.oops.mensa.model;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private String name;
    private List<Meal> meals;

    public Line() {
        this.meals = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public void addMeal(Meal meal) {
        this.meals.add(meal);
    }
}
