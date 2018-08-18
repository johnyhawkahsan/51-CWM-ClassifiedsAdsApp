package com.ahsan.a51_cwm_classifiedsadsapp.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ahsan.a51_cwm_classifiedsadsapp.R;
import com.ahsan.a51_cwm_classifiedsadsapp.SearchActivity;
import com.ahsan.a51_cwm_classifiedsadsapp.SearchFragment;
import com.ahsan.a51_cwm_classifiedsadsapp.WatchListFragment;
import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;

import java.util.ArrayList;

/**
 * Created by ahsan on 8/15/2018.
 */

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder>{

    private static final String TAG = "PostListAdapter";
    private static final int NUM_GRID_COLUMNS = 3;

    private ArrayList<Post> mPosts;
    private Context mContext;







    //===================================ViewHolder class for RecyclerView=========================================//
    public class ViewHolder extends RecyclerView.ViewHolder{

        //We're only using one Widget (ImageView) in our program
        SquareImageView mPostImage;

        //Constructor for ViewHolder
        public ViewHolder(View itemView) {
            super(itemView);

            //Instead of normal image view, we pass our manually created SquareImageView
            mPostImage = (SquareImageView) itemView.findViewById(R.id.post_image);

            //Now we need to explicitly set the size of the image view widgets = Mitch thinks it makes images load faster
            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels; //Gets the absolute width of the device screen
            int imageWidth = gridWidth/NUM_GRID_COLUMNS;
            mPostImage.setMaxHeight(imageWidth); //Setting same size for imageHeight and Width
            mPostImage.setMaxWidth(imageWidth); //Setting same size for imageHeight and Width
        }
    }
    //=================================//ViewHolder class for RecyclerView=========================================//



    //Default constructor for PostListAdapter
    public PostListAdapter(Context mContext, ArrayList<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    //Create ViewHolder object and pass layout to ViewHolder object
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_view_post, parent, false);
        return new ViewHolder(view);
    }

    //Display the data at specified position.
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        UniversalImageLoader.setImage(mPosts.get(position).getImage(), holder.mPostImage);

        final int pos = position;
        //TODO: When user clicks the image, it should reidrect him to another activity or fragment with post details.
        holder.mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: selected a post at position = " + holder.getAdapterPosition());

                //View the post in more detail by calling "ViewPostFragment".
                //Note that it's either called from "SearchFragment" #0 or "WatchListFragment" #1
                Fragment fragment = (Fragment) ((SearchActivity)mContext).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                ((SearchActivity) mContext).mViewPager.getCurrentItem()); //This will get the tag number of the calling fragment.

                Log.d(TAG, "onClick: Fragment tag = " + ((SearchActivity)mContext).mViewPager.getCurrentItem());

                if (fragment != null){
                    //If ViewPostFragment called from "SearchFragment" #0
                    if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){
                        Log.d(TAG, "onClick: switching from SearchFragment #0 to :" + mContext.getString(R.string.fragment_view_post));

                        //Create a new object of SearchFragment
                        SearchFragment searchFragment = (SearchFragment) ((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity) mContext).mViewPager.getCurrentItem());
                        searchFragment.viewPost(mPosts.get(pos).getPost_id());//get(pos) gets us single Post object and we use getPost_id() method of Post Object

                    }
                    //If ViewPostFragment called from "WatchListFragment" #1
                    else if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){
                        Log.d(TAG, "onClick: switching from WatchListFragment #1 to :" + mContext.getString(R.string.fragment_view_post));

                        //Create a new object of SearchFragment
                        WatchListFragment watchListFragment = (WatchListFragment) ((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity) mContext).mViewPager.getCurrentItem());
                        watchListFragment.viewPost(mPosts.get(pos).getPost_id());//get(pos) gets us single Post object and we use getPost_id() method of Post Object

                    }
                }

            }
        });
    }

    //Get total items in mPosts ArrayList
    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
