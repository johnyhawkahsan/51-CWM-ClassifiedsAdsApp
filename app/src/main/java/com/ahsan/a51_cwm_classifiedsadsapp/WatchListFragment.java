package com.ahsan.a51_cwm_classifiedsadsapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;
import com.ahsan.a51_cwm_classifiedsadsapp.util.PostListAdapter;
import com.ahsan.a51_cwm_classifiedsadsapp.util.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class WatchListFragment extends Fragment{

    private static final String TAG = "WatchListFragment";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int GRID_ITEM_MARGIN = 5;

    //widgets
    private RecyclerView mRecyclerView;
    private FrameLayout mFrameLayout;

    //vars
    private PostListAdapter mPostListAdapter;
    private ArrayList<Post> mPosts;
    private ArrayList<String> mPostsIds;
    private DatabaseReference mReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.watchListRecyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.watch_list_container);

        init();

        return view;
    }


    private void init() {
        Log.d(TAG, "init: initializing");
        mPosts = new ArrayList<>();
        mPostsIds = new ArrayList<>();

        setupPostsList();//Setup RecyclerView

        //reference for listening when items are added or removed from the watch list
        mReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_watch_list))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //set the listener to the reference
        mReference.addValueEventListener(mValueEventListener);
    }

    //Setup RecyclerView with posts
    public void setupPostsList(){
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS); //This class is used to add margins to images appearing in Recycler view
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS); //Item is dis
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mPostListAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mPostListAdapter);
    }


    //Value event listener to listen for items added or removed from database
    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch list node");
            getWatchListIds();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "onCancelled: databaseError = " + databaseError );
        }
    };


    //get watch list items id's
    private void getWatchListIds(){
        Log.d(TAG, "getWatchListIds: getting users watch list.");
        if (mPosts != null){
            mPosts.clear();
        }
        if (mPostsIds != null){
            mPostsIds.clear();
        }

        //Search Database node watch_list to get all id's stored with the current user
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(getString(R.string.node_watch_list))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //We need to get all id's stored within this node, but first need to check if there is any data within WatchList yet, because if not checked, this will lead to a crash
                if (dataSnapshot.getChildren().iterator().hasNext()){
                    Log.d(TAG, "onDataChange: hasNext()");
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for (DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.child(getString(R.string.field_post_id)).getValue().toString(); //Under Uid, there are multiple post id's, so we need to retrieve each one of them.
                        Log.d(TAG, "onDataChange: found post id: " + id);
                        mPostsIds.add(id);
                    }

                    Log.d(TAG, "onDataChange: mPostsIds array list size is = " + mPostsIds.size());
                    //get all posts based on post id's we found up here
                    getPosts();

                } else {
                    Log.d(TAG, "onDataChange: else means no posts are available at node " + R.string.node_watch_list);
                    getPosts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError :" + databaseError );
            }
        });
    }



    //get all posts based on post id's we found up here
    private void getPosts(){
        Log.d(TAG, "getPosts: getting posts for found watch list ids");
        if (mPostsIds.size() > 0){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            //=====================Iterate through each post id and get data associated with that id===================
            for(int i  = 0; i < mPostsIds.size(); i++){
                Log.d(TAG, "getPosts: getting post information for: " + mPostsIds.get(i));

                Query query = databaseReference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mPostsIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //There should be only one result in each iteration, so
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = singleSnapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post associated with id = " + post.toString());
                        mPosts.add(post);
                        mPostListAdapter.notifyDataSetChanged();//Update adapter
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: databaseError :" + databaseError );
                    }
                });
            }
        } else {
            mPostListAdapter.notifyDataSetChanged();//Still need to notify the adapter if the list is empty
        }
    }



    //This method is launched by "PostListAdapter", wo it is initiated by creating an object of "WatchListFragment" and it's called from there.
    public void viewPost(String postId){
        ViewPostFragment mViewPostFragment = new ViewPostFragment(); //New object of ViewPostFragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        //Pass bundle for new fragment launch
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        mViewPostFragment.setArguments(args);

        //watch_list_container is contained within "fragment_watch_list" and by default is gone.
        //Please note that this is different than "viewpager_container" which is located in "activity_search".
        transaction.replace(R.id.watch_list_container, mViewPostFragment, getString(R.string.fragment_view_post) );
        transaction.addToBackStack(getString(R.string.fragment_view_post)); //means that the transaction will be remembered after it is committed, and will reverse its operation when later popped off the stack.
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);//By default FrameLayout is invisible and RecyclerViw is visible.
    }




    //We need to destroy mValueEventListener so it may stop listening when this fragment is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        mReference.removeEventListener(mValueEventListener);
    }
}
