package com.ahsan.a51_cwm_classifiedsadsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;
import com.ahsan.a51_cwm_classifiedsadsapp.util.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class ViewPostFragment extends Fragment{

    private static final String TAG = "ViewPostFragment";

    //widgets
    private TextView mContactSeller, mTitle, mDescription, mPrice, mLocation, mSavePost;
    private ImageView mClose, mWatchList, mPostImage;

    //vars
    private String mPostId; //To store the post id we receive from the calling fragment in arguments
    private Post mPost;

    //We need to import method onCreate because according to mitch, it's called before onCreateView
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.arg_post_id));
        Log.d(TAG, "onCreate: we received (in arguments) the post id: " + mPostId);

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mContactSeller = (TextView) view.findViewById(R.id.post_contact);
        mTitle = (TextView) view.findViewById(R.id.post_title);
        mDescription = (TextView) view.findViewById(R.id.post_description);
        mPrice = (TextView) view.findViewById(R.id.post_price);
        mLocation = (TextView) view.findViewById(R.id.post_location);
        mClose = (ImageView) view.findViewById(R.id.post_close);
        mWatchList = (ImageView) view.findViewById(R.id.add_watch_list);
        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mSavePost = (TextView) view.findViewById(R.id.save_post);

        init();

        hideSoftKeyboard(); //Hide software keyboard from the display

        return view;
    }

    //Our main init method
    private void init() {
        getPostInfo(); //Search FireBase database based on the passed mPostId
        contactSellerMethod();//Contact seller method initiates an email activity

        //Close button method
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing post.");
                getActivity().getSupportFragmentManager().popBackStack();//Pop the top state off the back stack
            }
        });
        mClose.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_x_white));
        //mClose.setImageBitmap(createOutline(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_x_white))); //Not using this method
        //mClose.setColorFilter(Color.BLACK);

        mSavePost.setShadowLayer(5, 0, 0, Color.BLACK);//Gives the text a shadow of the specified blur radius and color, the specified distance from its drawn position.

        mWatchList.setImageBitmap(createOutline(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_save_white))); //Manually setting icon for mWatchList
        mWatchList.setColorFilter(Color.BLACK);//Set a tinting option for the image.



        //Methods copied from PostListAdapter
        //This differentiates between our fragments
        Fragment fragment = (Fragment) ((SearchActivity)getActivity()).getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                        ((SearchActivity) getActivity()).mViewPager.getCurrentItem()); //This will get the tag number of the calling fragment.

        Log.d(TAG, "onClick: Fragment tag = " + ((SearchActivity)getActivity()).mViewPager.getCurrentItem());

        //Differentiate between fragments by comparing tags.
        if (fragment != null){
            //If "SearchFragment" #0
            if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){
                Log.d(TAG, "onClick: switching from SearchFragment #0 to :" + getActivity().getString(R.string.fragment_view_post));

                //In SearchFragment, we need our "SaveButton" to work like this
                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addItemToWatchList();
                        Log.d(TAG, "onClick: addItemToWatchList()");
                    }
                });

                //Image View mWatchList does the same function as the above text view mSavePost
                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addItemToWatchList();
                        Log.d(TAG, "onClick: addItemToWatchList()");
                    }
                });
            }


            //If "WatchListFragment" #1
            else if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){
                Log.d(TAG, "onClick: switching from WatchListFragment #1 to :" + getActivity().getString(R.string.fragment_watch_list));

                mSavePost.setText("remove post");
                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItemFromWatchList();
                        Log.d(TAG, "onClick: removeItemFromWatchList()");
                        getActivity().getSupportFragmentManager().popBackStack(); //After removing item from Database, go back to calling activity.
                    }
                });
                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItemFromWatchList();
                        Log.d(TAG, "onClick: removeItemFromWatchList()");
                        getActivity().getSupportFragmentManager().popBackStack(); //After removing item from Database, go back to calling activity.
                    }
                });

            }
        }


    }

    //When save button is clicked, add item to another node within fireBase database
    private void addItemToWatchList(){
        Log.d(TAG, "addItemToWatchList: adding item to watch list.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_watch_list)) //Create new node named watch_list for "Cart" like items
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) //Save current user's display name
                .child(mPostId) //save post id
                .child(getString(R.string.field_post_id))//Last item in the node will be our postId
                .setValue(mPostId);
        Toast.makeText(getActivity(), "Added to watch list", Toast.LENGTH_SHORT).show();
    }

    //When save button is clicked from Watch List, remove item from fireBase database
    private void removeItemFromWatchList(){
        Log.d(TAG, "removeItemFromWatchList: removing item from watch list.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_watch_list))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .removeValue();
        Toast.makeText(getActivity(), "Removed from watch list", Toast.LENGTH_SHORT).show();
    }


    //Contact seller method initiates an email activity
    private void contactSellerMethod() {
        mContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {mPost.getContact_email()}); //add contact email details of seller
                getActivity().startActivity(emailIntent);
            }
        });
    }


    //Search FireBase database based on the passed mPostId
    private void getPostInfo(){
        Log.d(TAG, "getPostInfo: getting the post information.");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(mPostId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next(); //Because we only need one value not multiple
                if (singleSnapshot != null){
                    mPost = singleSnapshot.getValue(Post.class); //Assign object to the found data from FireBase.
                    Log.d(TAG, "onDataChange: found the post : " + mPost.toString());

                    mTitle.setText(mPost.getTitle());
                    mDescription.setText(mPost.getDescription());

                    String price = "FREE";
                    if (mPost.getPrice() != null){
                        price = "$" + mPost.getPrice();
                    }
                    mPrice.setText(price);

                    String location = mPost.getCity() + ", " + mPost.getState_province() + ", " + mPost.getCountry();
                    mLocation.setText(location);

                    UniversalImageLoader.setImage(mPost.getImage(), mPostImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError" + databaseError );
            }
        });
    }




    //Closing the keyboard when we navigate to this fragment
    private void hideSoftKeyboard(){
        final Activity activity = getActivity();
        final InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View focusedView = getActivity().getCurrentFocus();

        //I'm using this check here because before it was creating some problems because of using wrong container.
        if (focusedView != null){
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        else {
            Log.e(TAG, "hideSoftKeyboard: Focused view is null for some reason" );
        }
    }

    //Method to create outline - But I don't really like this method.
    public Bitmap createOutline(Bitmap src){
        Paint paint = new Paint();
        paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER));
        return  src.extractAlpha(paint, null);
    }
}
