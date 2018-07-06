package com.oops.mensa.model;

import java.util.ArrayList;
import java.util.List;

public class Plan {
    private List<Canteen> canteens;

    public Plan() {
        this.canteens = new ArrayList<>();
    }

    public Canteen getCanteen(String name) {
        for (Canteen can : canteens) {
            if (can.getName().equals(name)) {
                return can;
            }
        }
        return null;
    }

    public List<Canteen> getCanteens() {
        return canteens;
    }

    public void setCanteens(List<Canteen> canteens) {
        this.canteens = canteens;
    }

    public void addCanteen(Canteen canteen) {
        this.canteens.add(canteen);
    }
}
