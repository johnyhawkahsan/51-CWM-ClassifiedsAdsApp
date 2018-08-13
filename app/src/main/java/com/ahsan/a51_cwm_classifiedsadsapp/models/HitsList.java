package com.ahsan.a51_cwm_classifiedsadsapp.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ahsan on 8/13/2018.
 */

@IgnoreExtraProperties
public class HitsList {

    @SerializedName("hits")
    @Expose
    private List<PostSource> postIndex;

    public List<PostSource> getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(List<PostSource> postIndex) {
        this.postIndex = postIndex;
    }
}
