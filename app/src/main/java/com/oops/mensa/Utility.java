package com.oops.mensa;

import android.text.TextUtils;
import android.util.Base64;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.oops.mensa.model.Canteen;
import com.oops.mensa.model.Day;
import com.oops.mensa.model.Line;
import com.oops.mensa.model.Meal;
import com.oops.mensa.model.Plan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Utility {

    public static String convertCanteenName(String name) {
        String niceName = "";
        switch (name) {
            case "moltke":
                niceName = "Mensa Moltkestraße";
                break;
            case "adenauerring":
                niceName = "Mensa am Adenauerring";
                break;
            case "gottesaue":
                niceName = "Mensa Schloss Gottesaue";
                break;
            case "holzgarten":
                niceName = "Mensa Holzgartenstraße";
                break;
            case "erzberger":
                niceName = "Mensa Erzbergerstraße";
                break;
            case "tiefenbronner":
                niceName = "Mensa Hochschule Pforzheim";
                break;
            case "x1moltkestrasse":
                niceName = "Cafeteria Moltkestraße 30";
                break;
            default:
                niceName = name;
        }
        return niceName;
    }

    public static String convertLineName(String name) {
        String niceName = "";
        switch (name) {
            case "l1":
                niceName = "Linie 1";
                break;
            case "l2":
                niceName = "Linie 2";
                break;
            case "l3":
                niceName = "Linie 3";
                break;
            case "l45":
                niceName = "Linie 4/5";
                break;
            case "schnitzelbar":
                niceName = "Schnitzelbar";
                break;
            case "update":
                niceName = "Linie 6";
                break;
            case "abend":
                niceName = "Abend";
                break;
            case "aktion":
                niceName = "Aktionstheke";
                break;
            case "heisstheke":
                niceName = "Cafeteria Heißtheke";
                break;
            case "nmtisch":
                niceName = "Cafeteria ab 14:30";
                break;
            case "wahl1":
                niceName = "Wahlessen 1";
                break;
            case "wahl2":
                niceName = "Wahlessen 2";
                break;
            case "wahl3":
                niceName = "Wahlessen 3";
                break;
            case "gut":
                niceName = "Gut & Günstig";
                break;
            case "buffet":
                niceName = "Buffet";
                break;
            case "curryqueen":
                niceName = "Curry Queen";
                break;
            case "gut2":
                niceName = "Gut & Günstig 2";
                break;
            default:
                niceName = name;
        }
        return niceName;
    }


    //remove invalid chars in db path
    private static String parseString(String string) {
        return string.replace(".", "").replace("1/2", "Halbes")
                .replace("/", "").replace("#", "")
                .replace("$", "").replace("[", "")
                .replace("]", "");
    }


    public static String parseTime(Calendar calendar) {
        final String[] weekday = {"So.", "Mo.", "Di.", "Mi.", "Do.", "Fr.", "Sa."};
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

        Calendar today = Calendar.getInstance(timeZone);
        if (today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
            //show today
            return weekday[today.get(Calendar.DAY_OF_WEEK) - 1] + " (Heute)";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
            dateFormat.setTimeZone(calendar.getTimeZone());
            return weekday[calendar.get(Calendar.DAY_OF_WEEK) - 1] + " "
                    + "(" + dateFormat.format(calendar.getTime()) + ")";
        }
    }

    public static void sendHttpRequest(okhttp3.Callback callback) {
        String url = "https://www.sw-ka.de/json_interface/canteen/";
        String username = "jsonapi";
        String password = "AhVai6OoCh3Quoo6ji";

        String authString = username + ":" + password;
        String basicAuth = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Authorization", basicAuth)
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static Plan parsePlanResponse(String response) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        if(!TextUtils.isEmpty(response)) {
            try {
                JSONObject planObject = new JSONObject(response);
                Plan plan = new Plan();

                Iterator<String> iter = planObject.keys();
                while (iter.hasNext()) {
                    String canteenName = iter.next();

                    if (canteenName.equals("x1moltkestrasse")) {
                        //don't store this
                        continue;
                    }

                    if ( planObject.get(canteenName) instanceof JSONObject ) {
                        JSONObject canteenObject = (JSONObject) planObject.get(canteenName);
                        Canteen canteen = new Canteen();
                        canteen.setName(canteenName);

                        Iterator<String> iterCanteen = canteenObject.keys();
                        while (iterCanteen.hasNext()) {
                            String date = iterCanteen.next();
                            if ( canteenObject.get(date) instanceof JSONObject ) {
                                JSONObject dayObject = (JSONObject) canteenObject.get(date);
                                Day day = new Day();
                                day.setName(date);


                                Iterator<String> iterDay = dayObject.keys();
                                while (iterDay.hasNext()) {
                                    String lineName = iterDay.next();
                                    if (dayObject.get(lineName) instanceof JSONArray) {
                                        JSONArray lineObject = (JSONArray) dayObject.get(lineName);

                                        if (lineObject.getJSONObject(0).has("nodata")
                                                || lineObject.getJSONObject(0).has("closing_start")) {
                                            //for some weird JSON...
                                            break;
                                        }

                                        Line line = new Line();
                                        line.setName(lineName);

                                        for (int i = 0; i < lineObject.length(); i++) {
                                            JSONObject mealObject = lineObject.getJSONObject(i);
                                            Meal meal = new Meal();
                                            //remove dot so it can be used as db key
                                            meal.setMeal(parseString(mealObject.getString("meal")));
                                            meal.setDish(mealObject.getString("dish"));

                                            List<String> additives = new ArrayList<>();
                                            for (int j = 0; j < mealObject.getJSONArray("add").length(); j++) {
                                                additives.add(mealObject.getJSONArray("add").getString(j));
                                            }
                                            meal.setAdditives(additives);

                                            List<Double> prices = new ArrayList<>();
                                            prices.add(Double.parseDouble(mealObject.getString("price_1")));
                                            prices.add(Double.parseDouble(mealObject.getString("price_2")));
                                            prices.add(Double.parseDouble(mealObject.getString("price_3")));
                                            prices.add(Double.parseDouble(mealObject.getString("price_4")));
                                            meal.setPrices(prices);

                                            line.addMeal(meal);
                                            FirebaseUtil.checkToAddMeal(db, meal.getMeal());
                                        }

                                        day.addLine(line);
                                    }
                                }


                                canteen.addDay(day);
                            }
                        }


                        plan.addCanteen(canteen);
                    }
                }
                return plan;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
