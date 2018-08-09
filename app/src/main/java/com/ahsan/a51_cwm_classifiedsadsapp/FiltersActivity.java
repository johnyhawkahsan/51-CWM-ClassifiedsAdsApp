package com.ahsan.a51_cwm_classifiedsadsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by ahsan on 8/9/2018.
 */

public class FiltersActivity extends AppCompatActivity {

    private static final String TAG = "FiltersActivity";

    //Widgets
    private Button mSave;
    private EditText mCity, mStateProvince, mCountry;
    private ImageView mBackArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        mSave = (Button) findViewById(R.id.btnSave);
        mCity = (EditText) findViewById(R.id.input_city);
        mStateProvince = (EditText) findViewById(R.id.input_state_province);
        mCountry = (EditText) findViewById(R.id.input_country);
        mBackArrow = (ImageView) findViewById(R.id.backArrow);

        //get already saved preferences
        getFilterPreferences();

        //When save button is clicked, the data within editText will be saved to those fields
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: saving....");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FiltersActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                Log.d(TAG, "onClick: city: " + mCity.getText().toString());
                editor.putString(getString(R.string.preference_city), mCity.getText().toString());
            }
        });
        
    }

    private void getFilterPreferences() {
        Log.d(TAG, "getFilterPreferences: retrieving saved preferences.");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String country = preferences.getString(getString(R.string.preference_country), "");
        String state_province = preferences.getString(getString(R.string.preference_state_province), "");
        String city = preferences.getString(getString(R.string.preference_city), "");

    }
}
