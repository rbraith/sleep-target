package com.rbraithwaite.sleepapp.ui.stats.common;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

// REFACTOR [21-06-5 3:48PM] -- move logic from RangeSelectorController into here, then get rid
//  of RangeSelectorController.
public class RangeSelectorComponent
        extends ConstraintLayout
{
//*********************************************************
// constructors
//*********************************************************

    public RangeSelectorComponent(@NonNull Context context)
    {
        super(context);
        // REFACTOR [21-06-5 3:49PM] -- make a BaseConstraintLayoutComponent which handles
        //  these initComponent calls?
        initComponent(context);
    }
    
    public RangeSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public RangeSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public RangeSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent(context);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initComponent(Context context)
    {
        inflate(context, R.layout.stats_range_selector, this);
    }
}
