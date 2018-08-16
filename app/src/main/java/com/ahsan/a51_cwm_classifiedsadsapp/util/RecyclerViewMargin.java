package com.ahsan.a51_cwm_classifiedsadsapp.util;

import android.support.v7.widget.RecyclerView;

/**
 * Created by ahsan on 8/15/2018.
 */

public class RecyclerViewMargin extends RecyclerView.ItemDecoration{

    private final int columns;
    private int margin;

    public RecyclerViewMargin(int columns, int margin) {
        this.columns = columns;
        this.margin = margin;
    }

    

}
