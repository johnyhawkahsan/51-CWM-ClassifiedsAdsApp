package com.ahsan.a51_cwm_classifiedsadsapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ahsan.a51_cwm_classifiedsadsapp.util.UniversalImageLoader;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class PostFragment extends Fragment{

    private static final String TAG = "PostFragment";

    //widgets
    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mStateProvince, mCity, mContactEmail;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);
        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mTitle = (EditText) view.findViewById(R.id.input_title);
        mDescription = (EditText) view.findViewById(R.id.input_description);
        mPrice = (EditText) view.findViewById(R.id.input_price);
        mCountry = (EditText) view.findViewById(R.id.input_country);
        mStateProvince = (EditText) view.findViewById(R.id.input_state_province);
        mCity = (EditText) view.findViewById(R.id.input_city);
        mContactEmail = (EditText) view.findViewById(R.id.input_email);
        mPost = (Button) view.findViewById(R.id.btn_post);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();

        return view;
    }


    private void init(){

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");

            }
        });
    }

    //Reset all fields to empty
    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage); // setting empty image url sets null image
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mCountry.setText("");
        mStateProvince.setText("");
        mCity.setText("");
        mContactEmail.setText("");
    }

    //By default, the progress bar is invisible
    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    //first checking if the progress bar is visible is good programming practice
    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


}
