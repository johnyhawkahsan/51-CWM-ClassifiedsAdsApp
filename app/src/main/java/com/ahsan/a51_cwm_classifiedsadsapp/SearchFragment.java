package com.ahsan.a51_cwm_classifiedsadsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsan.a51_cwm_classifiedsadsapp.models.HitsList;
import com.ahsan.a51_cwm_classifiedsadsapp.models.HitsObject;
import com.ahsan.a51_cwm_classifiedsadsapp.models.Post;
import com.ahsan.a51_cwm_classifiedsadsapp.util.ElasticSearchAPI;
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

    //widgets
    private ImageView mFilters;
    private EditText mSearchText;

    //vars
    private String mElasticSearchPassword; //We are not saving Password in our app, instead we created a node in FireBase and get from there.
    private String mPrefCity;
    private String mPrefStateProv;
    private String mPrefCountry;
    private ArrayList<Post> mPosts; //ArrayList of Posts

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        mFilters = (ImageView) view.findViewById(R.id.ic_search);
        mSearchText = (EditText) view.findViewById(R.id.input_search);

        getElasticSearchPassword();
        getFilters();

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
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);//Create new object of ElasticSearchAPI class

                    HashMap<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Authorization", Credentials.basic("user", mElasticSearchPassword)); //This is like inside Postman, using ->Authorization ->Basic-auth

                    //For search queries appended to Base URL. Example : _search?default_operator=AND&q=NIFA+city:peshawar+state_province:KPK
                    String searchString = "";
                    if (!mSearchText.getText().equals("")){
                        searchString = searchString + mSearchText.getText().toString() + "*";
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

                    Call<HitsObject> call = searchAPI.search(headerMap, "AND", searchString); //Default operator is "AND"
                    call.enqueue(new Callback<HitsObject>() {
                        @Override
                        public void onResponse(Call<HitsObject> call, Response<HitsObject> response) {

                            HitsList hitsList = new HitsList();
                            String jsonResponse = "";
                            try {
                                Log.d(TAG, "onResponse: server response: " + response.toString());

                                if (response.isSuccessful()){

                                    hitsList = response.body().getHits();
                                }else {
                                    jsonResponse = response.errorBody().string();
                                }

                                //Hits list is not itself a list, but it contains a List. Please visit -> model -> HitsList
                                Log.d(TAG, "onResponse: hits: " + hitsList);

                                for (int i = 0 ; i < hitsList.getPostIndex().size(); i++){

                                    Log.d(TAG, "onResponse: data: " + hitsList.getPostIndex().get(i).getPost().toString());

                                    mPosts.add(hitsList.getPostIndex().get(i).getPost());

                                }
                                Log.d(TAG, "onResponse: size of posts: " + mPosts.size());

                                //TODO: Setup the list of posts (Inside RecyclerView)


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


        return view;
    }

    //We stored our Password in FireBase Database's node, so our app does not store it. This is a safe method.
    private void getElasticSearchPassword(){
        Log.d(TAG, "getElasticSearchPassword: retrieving elasticsearch password.");

        //Query FireBase for password field
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("posts") //Mitch was using R.string.node_elasticsearch whose value is "elasticsearch", but my FireBase database contains "posts"
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
        getFilters();
    }
}
