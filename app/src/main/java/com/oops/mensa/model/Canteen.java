package com.oops.mensa.model;

import java.util.ArrayList;
import java.util.Calendar;

public class Canteen {
    private String name;
    private ArrayList<Day> days;

    public Canteen() {
        this.days = new ArrayList<>();
    }

    public Day getDay(Calendar date) {
        for (Day d : days) {
            if (d.getCalendar().equals(date)) {
                return d;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Day> getDays() {
        return days;
    }

    public void setDays(ArrayList<Day> days) {
        this.days = days;
    }

    public void addDay(Day day) {
        this.days.add(day);
    }
}
