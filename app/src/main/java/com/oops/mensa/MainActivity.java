package com.oops.mensa;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends FragmentActivity {

    public MyAdapter adapter;

    private ViewPager pager;

    private TextView title;

    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;

    private Button navButton;

    private static String selectedCanteen;

    private FirebaseAuth mAuth;

    private DisplayPlanFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        selectedCanteen = getIntent().getStringExtra("selected");

        adapter = new MyAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        fragment = (DisplayPlanFragment) adapter.instantiateItem(pager, pager.getCurrentItem());

        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.requestPlan();
            }
        });

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setEnabled(false);

        title = findViewById(R.id.title_canteen);
        title.setText(Utility.convertCanteenName(selectedCanteen));
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("DEBUG", "user: "+currentUser);
        if (currentUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("debug", "on complete listenr");
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                            } else {
                                // If sign in fails, display a message to the user.
                            }
                        }
                    });
        }
    }


    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public Fragment getItem(int position) {
            return DisplayPlanFragment.newInstance(position, selectedCanteen);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
            Calendar day = Calendar.getInstance(timeZone);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            int dayOfYear = day.get(Calendar.DAY_OF_YEAR);
            day.set(Calendar.DAY_OF_YEAR, dayOfYear + position);

            return Utility.parseTime(day);
        }

    }

}
