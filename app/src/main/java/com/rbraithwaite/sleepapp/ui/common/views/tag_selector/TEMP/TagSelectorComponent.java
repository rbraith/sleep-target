package com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TEMP;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

public class TagSelectorComponent
        extends ConstraintLayout
{
//*********************************************************
// constructors
//*********************************************************

    public TagSelectorComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public TagSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public TagSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public TagSelectorComponent(
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
        inflate(context, R.layout.tag_selector, this);
    }
}
