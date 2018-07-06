package com.oops.mensa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oops.mensa.database.MealItem;
import com.oops.mensa.database.Review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

public class DetailActivity extends AppCompatActivity {

    private SharedPreferences pref;

    private String mealName;

    private SwipeRefreshLayout swipeRefresh;

    private Button likeButton;
    private Button dislikeButton;
    private Button likeButtonChosen;
    private Button dislikeButtonChosen;

    private TextView likeNumber;
    private TextView dislikeNumber;

    private DatabaseReference db;

    private boolean like = false;
    private boolean dislike = false;

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList;

    private TextView emptyText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.oops.mensa.R.layout.activity_detail);

        Button navButton = findViewById(com.oops.mensa.R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mealName = getIntent().getStringExtra("meal");

        db = FirebaseDatabase.getInstance().getReference();

        pref = getSharedPreferences("savings", Context.MODE_PRIVATE);
        final String likeStatus = pref.getString(mealName, null);

        swipeRefresh = findViewById(com.oops.mensa.R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReviews();
                swipeRefresh.setRefreshing(false);
            }
        });

        String titleText = mealName;
        if (titleText.length() > 25) {
            titleText = titleText.substring(0, 25) + "...";
        }
        TextView title = findViewById(com.oops.mensa.R.id.title);
        title.setText(titleText);

        likeButtonChosen = findViewById(R.id.like_button_chosen);
        dislikeButtonChosen = findViewById(R.id.dislike_button_chosen);
        likeButton = findViewById(R.id.like_button);
        dislikeButton = findViewById(R.id.dislike_button);
        likeNumber = findViewById(R.id.like_number);
        dislikeNumber = findViewById(R.id.dislike_number);

        if (likeStatus != null) {
            if (likeStatus.equals("like")) {
                like = true;
                likeButton.setVisibility(GONE);
                likeButtonChosen.setVisibility(View.VISIBLE);
            } else if (likeStatus.equals("dislike")) {
                dislike = true;
                dislikeButton.setVisibility(GONE);
                dislikeButtonChosen.setVisibility(View.VISIBLE);
            }
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(mealName, "like");
                editor.apply();

                FirebaseUtil.addLikeStatus(db, mealName, "likes");
                like = true;
                if (dislike) {
                    dislike = false;
                    FirebaseUtil.removeLikeStatus(db, mealName, "dislikes");
                }

                likeButton.setVisibility(GONE);
                likeButtonChosen.setVisibility(View.VISIBLE);

                dislikeButton.setVisibility(View.VISIBLE);
                dislikeButtonChosen.setVisibility(GONE);
            }
        });

        likeButtonChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(mealName, null);
                editor.apply();

                like = false;
                FirebaseUtil.removeLikeStatus(db, mealName, "likes");

                likeButton.setVisibility(View.VISIBLE);
                likeButtonChosen.setVisibility(View.GONE);
            }
        });

        dislikeButtonChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(mealName, null);
                editor.apply();

                dislike = false;
                FirebaseUtil.removeLikeStatus(db, mealName, "dislikes");

                dislikeButton.setVisibility(View.VISIBLE);
                dislikeButtonChosen.setVisibility(View.GONE);
            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(mealName, "dislike");
                editor.apply();

                FirebaseUtil.addLikeStatus(db, mealName, "dislikes");
                dislike = true;
                if (like) {
                    like = false;
                    FirebaseUtil.removeLikeStatus(db, mealName, "likes");
                }

                likeButton.setVisibility(View.VISIBLE);
                likeButtonChosen.setVisibility(View.GONE);

                dislikeButton.setVisibility(View.GONE);
                dislikeButtonChosen.setVisibility(View.VISIBLE);
            }
        });


        ValueEventListener numberListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MealItem mealItem = dataSnapshot.getValue(MealItem.class);
                likeNumber.setText(""+mealItem.likes);
                dislikeNumber.setText(""+mealItem.dislikes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        db.child("meals").child(mealName).addValueEventListener(numberListener);


        Button rateButton = findViewById(R.id.rate_button);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RateActivity.class);
                intent.putExtra("meal", mealName);
                startActivity(intent);
            }
        });

        emptyText = findViewById(R.id.empty_text);

        recyclerView = findViewById(R.id.recycler_view);

        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(this, reviewList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        getReviews();
    }

    private void getReviews() {
        db.child("reviews").child(mealName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewList.clear();

                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    Review review = userSnapshot.getValue(Review.class);
                    reviewList.add(review);
                }

                Collections.reverse(reviewList);

                adapter.notifyDataSetChanged();

                if (reviewList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    emptyText.setVisibility(GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onRestart(){
        super.onRestart();
        getReviews();
    }

}