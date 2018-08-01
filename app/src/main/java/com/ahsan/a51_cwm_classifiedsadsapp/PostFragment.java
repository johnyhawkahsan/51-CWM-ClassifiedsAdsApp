package com.ahsan.a51_cwm_classifiedsadsapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class PostFragment extends Fragment{

    private static final String TAG = "PostFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);

        return view;
    }
}
