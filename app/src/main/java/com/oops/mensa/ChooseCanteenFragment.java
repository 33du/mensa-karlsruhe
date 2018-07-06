package com.oops.mensa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.oops.mensa.model.Canteen;
import com.oops.mensa.model.Plan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseCanteenFragment extends Fragment {

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> canteenList = new ArrayList<>();

    private List<String> niceCanteenList = new ArrayList<>();

    private String selectedCanteen = null;

    private SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.oops.mensa.R.layout.choose_canteen, container, false);

        listView = view.findViewById(com.oops.mensa.R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, niceCanteenList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pref = getActivity().getSharedPreferences("savings", Context.MODE_PRIVATE);

        if (getActivity() instanceof ChooseActivity) {
            selectedCanteen = pref.getString("selected", null);
            if (selectedCanteen != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("selected", selectedCanteen);
                startActivity(intent);
                getActivity().finish();
            }
        }

        Set<String> canteenSet = pref.getStringSet("canteens", null);
        if (canteenSet != null) {
            canteenList.addAll(canteenSet);
            for (String name : canteenList) {
                niceCanteenList.add(Utility.convertCanteenName(name));
            }
        } else {
            Utility.sendHttpRequest(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No internet connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String responseText = response.body().string();

                    final Plan plan = Utility.parsePlanResponse(responseText);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Canteen can : plan.getCanteens()) {
                                canteenList.add(can.getName());
                            }

                            for (String name : canteenList) {
                                niceCanteenList.add(Utility.convertCanteenName(name));
                            }
                            adapter.notifyDataSetChanged();

                            SharedPreferences.Editor editor = pref.edit();
                            Set<String> set = new HashSet<>();
                            set.addAll(canteenList);
                            editor.putStringSet("canteens", set);
                            editor.apply();
                        }
                    });
                }
            });
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCanteen = canteenList.get(i);

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("selected", selectedCanteen);
                editor.apply();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("selected", selectedCanteen);
                startActivity(intent);
                getActivity().finish();

            }
        });
    }
}
