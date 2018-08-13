package com.ahsan.a51_cwm_classifiedsadsapp.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * So the output of GET in Postman is in this form.
 * So, first "hits" is the object, then we have 2 ignored properties, then we have "hits" list
 * Within "hits" list, we have important "_source" field, which we define by "PostSource" object.
 "hits": {
     "total": 1,
     "max_score": 2.2506514,
     "hits": [{
             "_index": "posts",
             "_type": "post",
             "_id": "-LJlnupgPQr_DRnTcudI",
             "_score": 2.2506514,
             "_source": {
                         "city": "Peshawar",
                         "contact_email": "mails@nifa.org.pk",
                         "country": "mails@nifa.org.pk",
                         "description": "Nifa Peshawar",
                         "image": "https://firebasestorage.googleapis.com/v0/b/cwm-classifiedsadsapp.appspot.com/o/posts%2Fusers%2F34lyQpquTCMufHAKeq5U22i3lZl1%2F-LJlnupgPQr_DRnTcudI%2Fpost_image?alt=media&token=94b7eb57-a792-4a3f-8856-f4a0d0c0a25d",
                         "post_id": "-LJlnupgPQr_DRnTcudI",
                         "price": "1999",
                         "state_province": "KPK",
                         "title": "Nifa",
                         "user_id": "34lyQpquTCMufHAKeq5U22i3lZl1"
                        }
            }]
        }
 */

@IgnoreExtraProperties //Ignore extra unused properties in FireBase
public class PostSource {

    @SerializedName("_source")
    @Expose
    private Post post; //Create Post object, because essentially PostSource is a post itself

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
