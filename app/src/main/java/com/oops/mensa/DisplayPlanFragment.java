package com.oops.mensa;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oops.mensa.model.Line;
import com.oops.mensa.model.Meal;
import com.oops.mensa.model.Plan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class DisplayPlanFragment extends Fragment {

    private TextView text;

    private LinearLayout linearLayout;

    public String selectedCanteen;

    private Calendar date;

    private SharedPreferences pref;

    private Plan plan;

    public static DisplayPlanFragment newInstance(int position, String selectedCanteen) {
        DisplayPlanFragment f = new DisplayPlanFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("selectedCanteen", selectedCanteen);

        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        selectedCanteen = args.getString("selectedCanteen");
        int position = args.getInt("position");
        date = parsePositionToDate(position);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.display_plan, container, false);
        text = view.findViewById(R.id.text_view);
        linearLayout = view.findViewById(R.id.plan_layout);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }


    public void init() {
        pref = getActivity().getSharedPreferences("savings", Context.MODE_PRIVATE);
        String planString = pref.getString("plan", null);
        if (planString != null) {
            plan = Utility.parsePlanResponse(planString);
            displayPlan();
            ((MainActivity) getActivity()).swipeRefresh.setRefreshing(false);
        } else {
            requestPlan();
            ((MainActivity) getActivity()).swipeRefresh.setRefreshing(false);
        }
    }


    public void requestPlan() {
        Utility.sendHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "No internet connection",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();

                SharedPreferences pref = getContext().getSharedPreferences("savings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("plan", responseText);
                editor.apply();

                plan = Utility.parsePlanResponse(responseText);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayPlan();
                    }
                });
            }
        });
    }


    public void displayPlan() {
        if (plan.getCanteen(selectedCanteen).getDay(date) == null) {
            text.setText("Keine Informationen für diesen Tag.");
            text.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).swipeRefresh.setRefreshing(false);
            return;
        }

        ArrayList<Line> lines = plan.getCanteen(selectedCanteen).getDay(date).getLines();
        for (Line line : lines) {
            //add line name
            TextView lineName = new TextView(getActivity());
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(50, 80, 50, 50);
            lineName.setLayoutParams(titleParams);
            lineName.setText(Utility.convertLineName(line.getName()));
            if (selectedCanteen.equals("adenauerring") && line.getName().equals("aktion")) {
                lineName.setText("Curry Queen");
            }
            lineName.setTextSize(18);
            lineName.setTextColor(getResources().getColor(R.color.colorAccent, null));
            lineName.setTypeface(Typeface.DEFAULT_BOLD);
            linearLayout.addView(lineName);


            int mealCount = 0;
            for (Meal meal : line.getMeals()) {
                //layout for each meal
                LinearLayout mealLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mealLayout.setPadding(50, 25, 50, 25);

                mealLayout.setClickable(true);
                mealLayout.setFocusable(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    // If we're running on Honeycomb or newer, then we can use the Theme's
                    // selectableItemBackground to ensure that the View has a pressed state
                    TypedValue outValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    mealLayout.setBackgroundResource(outValue.resourceId);
                }

                final Meal selectedMeal = meal;
                mealLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra("meal", selectedMeal.getMeal());
                        startActivity(intent);
                    }
                });

                linearLayout.addView(mealLayout, lp);

                //layout for meal and dish name
                LinearLayout nameLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT);
                nameParams.weight = 0.8f;
                nameLayout.setOrientation(LinearLayout.VERTICAL);
                mealLayout.addView(nameLayout, nameParams);

                //layout for price
                LinearLayout priceLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT);
                priceParams.weight = 0.2f;
                mealLayout.addView(priceLayout, priceParams);

                //meal name inside nameLayout
                TextView mealName = new TextView(getActivity());
                RelativeLayout.LayoutParams mealParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mealName.setText(meal.getMeal());
                mealName.setId(mealCount);
                mealName.setTextColor(Color.BLACK);
                mealName.setTextSize(16);
                nameLayout.addView(mealName, mealParams);

                //dish name inside nameLayout
                if (!meal.getDish().equals("")) {
                    TextView dishName = new TextView(getActivity());
                    dishName.setText(meal.getDish());
                    dishName.setTextSize(14);
                    RelativeLayout.LayoutParams dishParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    nameLayout.addView(dishName, dishParams);
                }

                //price inside priceLayout
                TextView price = new TextView(getActivity());
                price.setText(String.format("%.2f", meal.getPrices().get(0)) + " €");
                price.setTextSize(16);
                RelativeLayout.LayoutParams priceTextParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                price.setGravity(Gravity.RIGHT);
                priceLayout.addView(price, priceTextParams);

                mealCount++;
            }

        }
        linearLayout.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).swipeRefresh.setRefreshing(false);
    }


    private Calendar parsePositionToDate(int position) {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Calendar day = Calendar.getInstance(timeZone);
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        int dayOfYear = day.get(Calendar.DAY_OF_YEAR);
        day.set(Calendar.DAY_OF_YEAR, dayOfYear + position);

        return day;
    }
}
