package com.ahsan.a51_cwm_classifiedsadsapp.util;

import com.ahsan.a51_cwm_classifiedsadsapp.models.HitsObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

/**
 * Created by Ahsan on 8/13/2018.
 */

public interface ElasticSearchAPI {
/*
//Have a closer look at what we're building here.
http://35.226.197.171/elasticsearch/posts/post/_search?default_operator=AND&q=NIFA+city:peshawar+state_province:KPK
 */

    @GET("_search/")
    Call<HitsObject> search( //This call will return HitsObject
            @HeaderMap Map<String, String> headers, //Header uses Basic Authorization model within SearchFragment
            @Query("default_operator") String operator, //1st query (automatically prepend '?'), we are going to use "AND" as default_operator
            @Query("q") String query //2nd query (automatically prepend '&'), this is what user put in search textBox
            );
}
