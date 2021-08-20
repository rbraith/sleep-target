package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

// HACK [21-08-17 11:55PM] -- This exists because I was struggling to get FlexboxLayoutManager
//  working correctly (it would extend off the screen and clip its content). TagSelectorComponent
//  was the only case where I got it working, so I duplicated that implementation here.
//  Basically I have no idea why making a component is the only way this works :(
public class PostSleepTagsComponent
        extends ConstraintLayout
{
//*********************************************************
// constructors
//*********************************************************

    public PostSleepTagsComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public PostSleepTagsComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public PostSleepTagsComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public PostSleepTagsComponent(
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

    // REFACTOR [21-05-25 1:38PM] -- This pattern is repeated in MoodSelectorComponent
    //  consider making a ConstraintLayoutComponent base class.
    private void initComponent(Context context)
    {
        inflate(context, R.layout.post_sleep_tags, this);
    }
}
