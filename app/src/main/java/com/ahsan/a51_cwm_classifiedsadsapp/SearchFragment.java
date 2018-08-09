package com.ahsan.a51_cwm_classifiedsadsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class SearchFragment extends Fragment{

    private static final String TAG = "SearchFragment";

    //widgets
    private ImageView mFilters;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mFilters = (ImageView) view.findViewById(R.id.ic_search);
        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to filters activity.");

                Intent intent = new Intent(getActivity(), FiltersActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
