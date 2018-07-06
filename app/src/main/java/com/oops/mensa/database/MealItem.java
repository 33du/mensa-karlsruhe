package com.oops.mensa.database;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MealItem {

    public long likes;
    public long dislikes;

    public MealItem() {}

    public MealItem(long likes, long dislikes) {
        this.likes = likes;
        this.dislikes = dislikes;
    }
}
