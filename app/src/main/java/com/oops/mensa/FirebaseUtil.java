package com.oops.mensa;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.oops.mensa.database.MealItem;

public class FirebaseUtil {
    static public void checkToAddMeal(final DatabaseReference db, final String mealName) {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(MealItem.class) == null) {
                    MealItem meal = new MealItem(0, 0);
                    db.child("meals").child(mealName).setValue(meal);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        db.child("meals").child(mealName).addListenerForSingleValueEvent(listener);
    }


    static public void addLikeStatus(final DatabaseReference db, final String mealName, final String likeStatus) {
        if (likeStatus.equals("likes") || likeStatus.equals("dislikes")) {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long currentValue = (long) dataSnapshot.getValue();
                    db.child("meals").child(mealName).child(likeStatus).setValue(currentValue + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            db.child("meals").child(mealName).child(likeStatus).addListenerForSingleValueEvent(listener);
        }
    }


    static public void removeLikeStatus(final DatabaseReference db, final String mealName, final String likeStatus) {
        if (likeStatus.equals("likes") || likeStatus.equals("dislikes")) {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long currentValue = (long) dataSnapshot.getValue();
                    db.child("meals").child(mealName).child(likeStatus).setValue(currentValue - 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            db.child("meals").child(mealName).child(likeStatus).addListenerForSingleValueEvent(listener);
        }
    }
}
