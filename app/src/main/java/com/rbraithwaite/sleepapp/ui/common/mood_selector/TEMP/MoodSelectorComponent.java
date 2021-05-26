package com.rbraithwaite.sleepapp.ui.common.mood_selector.TEMP;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

// REFACTOR [21-05-24 3:17AM] -- move MoodSelectorController functionality into here, move this
//  out of TEMP package and delete that package.
public class MoodSelectorComponent extends ConstraintLayout
{
    public MoodSelectorComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent(context);
    }
    
    private void initComponent(Context context)
    {
        inflate(context, R.layout.mood_selector, this);
    }
}
