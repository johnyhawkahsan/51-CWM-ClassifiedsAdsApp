package com.ahsan.a51_cwm_classifiedsadsapp;

import android.graphics.Bitmap;
import android.net.Uri;
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

public class PostFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener{

    private static final String TAG = "PostFragment";

    //widgets
    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mStateProvince, mCity, mContactEmail;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;



    //Important method after implementing OnPhotoSelectedListener
    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageViw using UniversalImageLoader");

        //Assign to a global variable
        mSelectedBitmap = null;
        mSelectedUri = imagePath;

        UniversalImageLoader.setImage(imagePath.toString(), mPostImage);//Using universalImageLoader, we set the image

    }

    //Important method after implementing OnPhotoSelectedListener
    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the captured image to imageView directly");

        //Assign to a global variable
        mSelectedBitmap = bitmap;
        mSelectedUri = null;

        mPostImage.setImageBitmap(bitmap); //Directly set image using bitmap- Note: we don't need to use ImageLoader here.

    }








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


    //When postImage image is clicked, launch dialog box to select either photo or either capture new photo
    private void init(){

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");

                //Now run the select photo dialog box
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this, 1); //Set where PhotoDialg fragment is going to send the data. Note: 1 is request code

                //TODO: NOTE: We are sending data directly

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
