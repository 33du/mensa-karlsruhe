package com.oops.mensa.model;

import java.util.ArrayList;
import java.util.Calendar;

public class Day {
    private String name;
    private ArrayList<Line> lines;
    private boolean isHoliday;

    public Day() {
        this.lines = new ArrayList<>();
    }

    public Calendar getCalendar() {
        long timeStamp = Long.parseLong(name) * 1000L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        return calendar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public void addLine(Line line) {
        this.lines.add(line);
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }
}
