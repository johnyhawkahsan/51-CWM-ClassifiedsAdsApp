package com.ahsan.a51_cwm_classifiedsadsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsan.a51_cwm_classifiedsadsapp.models.HitsList;
import com.ahsan.a51_cwm_classifiedsadsapp.models.HitsObject;
import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;
import com.ahsan.a51_cwm_classifiedsadsapp.util.ElasticSearchAPI;
import com.ahsan.a51_cwm_classifiedsadsapp.util.PostListAdapter;
import com.ahsan.a51_cwm_classifiedsadsapp.util.RecyclerViewMargin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ahsan on 8/1/2018.
 */

public class SearchFragment extends Fragment{

    private static final String TAG = "SearchFragment";
    private static final String BASE_URL = "http://35.226.197.171/elasticsearch/posts/post/";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int GRID_ITEM_MARGIN = 5;

    //widgets
    private ImageView mFilters;
    private EditText mSearchText;
    private RecyclerView mRecyclerView;
    private FrameLayout mFrameLayout; //By default, this is invisible, only visible when user clicks on an item

    //vars
    private String mElasticSearchPassword; //We are not saving Password in our app, instead we created a node in FireBase and get from there.
    private String mPrefCity;
    private String mPrefStateProv;
    private String mPrefCountry;
    private ArrayList<Post> mPosts; //ArrayList of Posts
    private PostListAdapter mPostListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        mFilters = (ImageView) view.findViewById(R.id.ic_search);
        mSearchText = (EditText) view.findViewById(R.id.input_search);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.frameContainer); //By default, this is invisible, only visible when user clicks on an item
        getElasticSearchPassword(); //Get elastic search password saved in our database and save in variable mElasticSearchPassword
        init();


        return view;
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


    //Setup method for Search icon and return key to initiate search
    public void init(){
        //When Search button is clicked, Launch FiltersActivity for setting Search filters in SharedPreferences
        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to filters activity.");

                Intent intent = new Intent(getActivity(), FiltersActivity.class);
                startActivity(intent);
            }
        });

        //User can press "Return" button to initiate search - Very great method because once user type query, instantly pressing "Return" initiates search
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                    //Now we need to create our Search Query
                    mPosts = new ArrayList<Post>();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL) //Set the API base URL.
                            .addConverterFactory(GsonConverterFactory.create()) //Add converter factory for serialization and deserialization of objects.
                            .build();
                    ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);//Create new object of ElasticSearchAPI class

                    HashMap<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Authorization", Credentials.basic("user", mElasticSearchPassword)); //This is like inside Postman, using ->Authorization ->Basic-auth

                    //For search queries appended to Base URL. Example : _search?default_operator=AND&q=NIFA+city:peshawar+state_province:KPK
                    String searchString = "";
                    if (!mSearchText.getText().equals("")){
                        searchString = searchString + mSearchText.getText().toString() + "*";//Append * at the end of search text, also when user doesn't type any text, * means all the data is displayed.
                    }
                    if (!mPrefCity.equals("")){
                        //NOTE: Leaving blank space before city, retrofit automatically adds + because we specified "AND" as our default operator, means every white space will be changed with + meaning AND
                        searchString = searchString + " city:" + mPrefCity;
                    }
                    if (!mPrefStateProv.equals("")){
                        searchString = searchString + " state_province:" + mPrefStateProv;
                    }
                    if (!mPrefCountry.equals("")){
                        searchString = searchString + " country:" + mPrefCountry;
                    }


                    //=======================Create a call to request Data========================================//

                    Call<HitsObject> call = searchAPI.search(headerMap, "AND", searchString); //Default operator is "AND"
                    call.enqueue(new Callback<HitsObject>() { //HitsObject are returned from ElasticSearchAPI request.
                        @Override
                        public void onResponse(Call<HitsObject> call, Response<HitsObject> response) {

                            HitsList hitsList = new HitsList(); //Create new HitsList object
                            String jsonResponse = "";
                            try {
                                Log.d(TAG, "onResponse: server response: " + response.toString());

                                if (response.isSuccessful()){

                                    hitsList = response.body().getHits(); //As the output is received in HitsObject form, we extract HitsList from it and assign to our own list.

                                }else {
                                    jsonResponse = response.errorBody().string();
                                    Log.e(TAG, "onResponse: error occurred while retrieving HitsObject : " + jsonResponse );
                                }

                                //Hits list is not itself a list, but it contains a PostSource List. Please visit -> model -> HitsList
                                Log.d(TAG, "onResponse: hits: " + hitsList);

                                //Iterate through the hitsList and add each PostSource item to our ArrayList
                                for (int i = 0 ; i < hitsList.getPostIndex().size(); i++){

                                    Log.d(TAG, "onResponse: data: " + hitsList.getPostIndex().get(i).getPost().toString());

                                    mPosts.add(hitsList.getPostIndex().get(i).getPost());

                                }

                                Log.d(TAG, "onResponse: size of posts: " + mPosts.size());

                                //Setup the list of posts (Inside RecyclerView) meaning display search result
                                setupPostsList();



                            } catch (NullPointerException e){
                                Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                            }catch (IndexOutOfBoundsException e){
                                Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                            }catch (IOException e){
                                Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                            }
                        }

                        @Override
                        public void onFailure(Call<HitsObject> call, Throwable t) {
                            Log.e(TAG, "onFailure: " + t.getMessage());
                            Toast.makeText(getActivity(), "Search Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return false;
            }
        });
    }


    //This method is used by "PostListAdapter", wo it is initiated by creating an object of "SearchFragment" and it's called from there.
    public void viewPost(String postId){
        ViewPostFragment mViewPostFragment = new ViewPostFragment(); //New object of ViewPostFragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        //Pass bundle for new fragment launch
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        mViewPostFragment.setArguments(args);

        //frameContainer is contained within "fragment_search" and by default is invisible.
        //Please note that this is different than "viewpager_container" which is located in "activity_search".
        transaction.replace(R.id.frameContainer, mViewPostFragment, getString(R.string.fragment_view_post) );
        transaction.addToBackStack(getString(R.string.fragment_view_post)); //means that the transaction will be remembered after it is committed, and will reverse its operation when later popped off the stack.
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);//By default FrameLayout is invisible and RecyclerViw is visible.

    }


    //We stored our Password in FireBase Database's node, so our app does not store it. This is a safe method.
    private void getElasticSearchPassword(){
        Log.d(TAG, "getElasticSearchPassword: retrieving elasticsearch password.");

        //Query FireBase for password field
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_posts)) //Mitch was using R.string.node_elasticsearch whose value is "elasticsearch", but my FireBase database contains "posts"
                .orderByValue();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(TAG, "onDataChange: dataSnapshot = " + dataSnapshot.getChildren().iterator().next());

                DataSnapshot singleSnapShot = dataSnapshot.getChildren().iterator().next();
                mElasticSearchPassword = singleSnapShot.getValue().toString();

                Log.d(TAG, "onDataChange: mElasticSearchPassword = " + mElasticSearchPassword);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError : " + databaseError );
            }
        });
    }

    //To get data stored in Shared Preferences
    private void getFilters(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefCity = preferences.getString(getString(R.string.preference_city), "");
        mPrefStateProv = preferences.getString(getString(R.string.preference_state_province), "");
        mPrefCountry = preferences.getString(getString(R.string.preference_country), "");

        Log.d(TAG, "getFilters: got filters: \ncity: " + mPrefCity + "\nState/Prov: " + mPrefStateProv + "\nCountry: " + mPrefCountry);
    }

    //We need to call getFilters() in onResume() because once user change filter and come back, we need to re access it.
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: run getFilters() method");
        getFilters();
    }
}
