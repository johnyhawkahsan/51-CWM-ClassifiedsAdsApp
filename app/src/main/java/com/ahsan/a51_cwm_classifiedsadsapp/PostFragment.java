package com.ahsan.a51_cwm_classifiedsadsapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;
import com.ahsan.a51_cwm_classifiedsadsapp.util.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    private byte[] mUploadBytes; //This is what we are going to upload to FireBase
    private double mProgress = 0; //Global variable for Upload progress within executeUploadTask()



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

        //When PostImage imageView is clicked, user is presented with dialog fragment to choose between 2 options
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");

                //Now run the select photo dialog box
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this, 1); //Set where PhotoDialog fragment is going to send the data. Note: 1 is request code
            }
        });


        //TODO: NOTE: We are sending data directly to FireBase
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to post....");
                if (!isEmpty(mTitle.getText().toString())
                        && !isEmpty(mDescription.getText().toString())
                        && !isEmpty(mPrice.getText().toString())
                        && !isEmpty(mCountry.getText().toString())
                        && !isEmpty(mStateProvince.getText().toString())
                        && !isEmpty(mCity.getText().toString())
                        && !isEmpty(mContactEmail.getText().toString()))
                {
                    //We have a bitmap and no Uri
                    if (mSelectedBitmap != null && mSelectedUri == null){
                        Log.d(TAG, "onClick: We have a bitmap and no Uri");
                        uploadNewPhoto(mSelectedBitmap);
                    }
                    //We have a Uri and no bitmap
                    else if (mSelectedBitmap == null && mSelectedUri != null){
                        Log.d(TAG, "onClick: We have a Uri and no bitmap");
                        uploadNewPhoto(mSelectedUri);
                    }

                } else
                {
                    //Anyone of the above field is empty
                    Toast.makeText(getActivity(), "You must full out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // (Two methods with same names?) Method overloading (or Function overloading) is legal in C++ and in Java, but only if the methods take a different arguments (i.e. do different things).
    //In this method, we have bitmap and Uri is null.
    private void uploadNewPhoto(Bitmap bitmap) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    //In this method, we have Uri and bitmap is null.
    private void uploadNewPhoto(Uri imagePath) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);//We are passing null image in this method
        resize.execute(imagePath);
    }








    //Now we need to compress the image in BackgroundTask before sending to FireBase
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]>{ //Uri = param, Integer = progress and byte[] = result
        Bitmap mBitmap; //If bitmap is available, mBitmap will be assigned within constructor. If null, mBitmap will be obtained from Uri

        //Constructor accepts Bitmap, which can either be bitmap or null - This if statement identifies if there is any bitmap
        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap != null){
                Log.d(TAG, "BackgroundImageResize: bitmap != null");
                this.mBitmap = bitmap; //If we are using Bitmap instead of Uri, our bitmap will not be null, so we will assign
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "Compressing image", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started compressing. params[0] = " + params[0]);

            //bitmap == null means we are passing uri here
            if (mBitmap == null){
                Log.d(TAG, "doInBackground: bitmap == null");
                try {
                    //If bitmap is null, means we are using Uri, so we need to get this image from Uri and convert and compress in bitmap form and then upload.
                    mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), params[0]); //params[0] contains our Uri

                } catch (IOException e){
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }

            //Convert this bitmap (we received using Uri) into byte array using the method we created below
            //Also if the image was not Uri and was bitmap, it will automatically skip if statement and does this below task.
            byte[] bytes = null;
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount() / 1000000);//To get image size before compression, dividing by 1000000 is size of mega bytes

            bytes = getBytesFromBitmap(mBitmap, 100); //This method is defined below
            Log.d(TAG, "doInBackground: megabytes after compression: " + bytes.length / 1000000);//To get image size after compression

            return bytes;
        }

        //After image is compressed in BackgroundImageResize task, upload the byte array to FireBase using upload task
        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes; //After byte array is received after getting bitmap and converting to byte[] array
            hideProgressBar();

            //execute the upload task after image is compressed in BackgroundImageResize task
            executeUploadTask();
        }
    }







    //To execute the upload task to FireBase
    public void executeUploadTask(){
        Toast.makeText(getActivity(), "Uploading image", Toast.LENGTH_SHORT).show();

        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey(); //Get this posts unique id
        Log.d(TAG, "executeUploadTask: postId = " + postId);

        //Save this post in directory posts/users/userID/postId/post_image
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                "/" + postId + "/post_image");

        Log.d(TAG, "executeUploadTask: storageReference = " + storageReference);

        UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(getActivity(), "Post Success", Toast.LENGTH_SHORT).show();

                //Insert the download urL for uploaded image into the FireBase database
                @SuppressWarnings("VisibleForTests") Uri firebaseUri = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "onSuccess: FireBase download url: " + firebaseUri.toString());


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                //Save this reference in Post object
                Post post = new Post();
                post.setImage(firebaseUri.toString()); //Image URL within FireBase
                post.setCity(mCity.getText().toString());
                post.setContact_email(mContactEmail.getText().toString());
                post.setCountry(mContactEmail.getText().toString());
                post.setDescription(mDescription.getText().toString());
                post.setPost_id(postId);
                post.setPrice(mPrice.getText().toString());
                post.setState_province(mStateProvince.getText().toString());
                post.setTitle(mTitle.getText().toString());
                post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid()); //Get current logged in users id

                //Save the object "post" data with title "posts" and then "PostId"
                reference.child(getString(R.string.node_posts))
                        .child(postId)
                        .setValue(post);

                resetFields();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), "Could not upload photo", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //TaskSnapshot was giving error "This method should only be accessed from tests or within private scope". Solved by this answer : https://stackoverflow.com/questions/41105586/android-firebase-tasksnapshot-method-should-only-be-accessed-within-privat

                //@SuppressWarnings("VisibleForTests") double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                //Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();

                //TODO: Because of the error, I used simple approach above so this is going to show progress too often now.
                //This code is used to prevent showing the progress too often.
                //if (currentProgress > mProgress + 15){
                    //mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //}

            }
        });

    }

    //Method to get byte array data when we pass bitmap to it.
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
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
     * @param string Input the string you want to test
     * @return true if empty and false if not empty
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }



}
