package com.oops.mensa.database;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@IgnoreExtraProperties
public class Review {
    public String text;
    public String pub_time;
    public String image_url;

    public Review() {}

    public Review(String text, String url) {
        this.text = text;
        this.image_url = url;

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Calendar time = Calendar.getInstance(timeZone);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        this.pub_time = format.format(time.getTime());
    }
}
