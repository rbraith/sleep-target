package com.rbraithwaite.sleepapp.ui.stats.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

import java.util.Optional;

// REFACTOR [21-06-5 3:48PM] -- move logic from RangeSelectorController into here, then get rid
//  of RangeSelectorController.
public class RangeSelectorComponent
        extends ConstraintLayout
{
//*********************************************************
// private properties
//*********************************************************

    private ImageButton mBackButton;
    private ImageButton mForwardButton;
    private ImageButton mMoreButton;
    private TextView mRangeValue;
    private Callbacks mCallbacks;
    
//*********************************************************
// public helpers
//*********************************************************

    public static abstract class Callbacks
    {
        public abstract int getMenuId();
        
        public abstract void onBackPressed();
        
        public abstract void onForwardPressed();
        
        public void onPopupMenuInflated(Menu popupMenu)
        {
            // do nothing
        }
        
        public boolean onPopupMenuItemClicked(MenuItem item)
        {
            return false;
        }
    }

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
    
//*********************************************************
// api
//*********************************************************

    public void setCallbacks(Callbacks callbacks)
    {
        mCallbacks = callbacks;
    }
    
    public void setRangeValueText(String text)
    {
        mRangeValue.setText(text);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initComponent(Context context)
    {
        inflate(context, R.layout.stats_range_selector, this);
        
        mBackButton = findViewById(R.id.stats_range_selector_back);
        mForwardButton = findViewById(R.id.stats_range_selector_forward);
        mMoreButton = findViewById(R.id.stats_range_selector_more);
        mRangeValue = findViewById(R.id.stats_range_selector_value);
        
        mBackButton.setOnClickListener(v -> getOptionalCallbacks().ifPresent(Callbacks::onBackPressed));
        mForwardButton.setOnClickListener(v -> getOptionalCallbacks().ifPresent(Callbacks::onForwardPressed));
        mMoreButton.setOnClickListener(v -> getOptionalCallbacks().ifPresent(callbacks -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.inflate(callbacks.getMenuId());
            callbacks.onPopupMenuInflated(popup.getMenu());
            popup.setOnMenuItemClickListener(callbacks::onPopupMenuItemClicked);
            popup.show();
        }));
    }
    
    private Optional<Callbacks> getOptionalCallbacks()
    {
        return Optional.ofNullable(mCallbacks);
    }
}
