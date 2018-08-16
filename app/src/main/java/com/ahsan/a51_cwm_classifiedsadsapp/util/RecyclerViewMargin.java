package com.ahsan.a51_cwm_classifiedsadsapp.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ahsan on 8/15/2018.
 */

public class RecyclerViewMargin extends RecyclerView.ItemDecoration{

    private final int columns;
    private int margin;

    //Constructor for class - We need to set columns and margin while creating the object, so it sets margin for our imageView within RecyclerView.
    public RecyclerViewMargin(int columns, int margin) {
        this.columns = columns;
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        outRect.right = margin;  //apply margins to right side of rectangle
        outRect.bottom = margin; //apply margins to the bottom of rectangle

        //apply margins to the top of rectangle only if the position value is below no of columns
        if (position < columns){
            outRect.top = margin;
        }

        //apply margins to the left of rectangle only if the remainder is zero
        if (position % columns == 0){
            outRect.left = margin;
        }

    }
}
