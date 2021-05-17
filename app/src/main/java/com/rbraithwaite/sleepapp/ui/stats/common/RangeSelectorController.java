package com.rbraithwaite.sleepapp.ui.stats.common;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.rbraithwaite.sleepapp.R;

public abstract class RangeSelectorController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private ImageButton mBackButton;
    private ImageButton mForwardButton;
    private ImageButton mMoreButton;
    private TextView mRangeValue;
    
//*********************************************************
// constructors
//*********************************************************

    public RangeSelectorController(View root)
    {
        mRoot = root;
        mBackButton = mRoot.findViewById(R.id.stats_range_selector_back);
        mForwardButton = mRoot.findViewById(R.id.stats_range_selector_forward);
        mMoreButton = mRoot.findViewById(R.id.stats_range_selector_more);
        mRangeValue = mRoot.findViewById(R.id.stats_range_selector_value);
        
        mBackButton.setOnClickListener(v -> onBackPressed());
        mForwardButton.setOnClickListener(v -> onForwardPressed());
        mMoreButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mRoot.getContext(), v);
            popup.inflate(getMenuId());
            onPopupMenuInflated(popup.getMenu());
            popup.setOnMenuItemClickListener(this::onPopupMenuItemClicked);
            popup.show();
        });
    }
    
//*********************************************************
// abstract
//*********************************************************

    public abstract int getMenuId();
    
    public abstract void onBackPressed();
    
    public abstract void onForwardPressed();
    
//*********************************************************
// api
//*********************************************************

    public void setText(String text)
    {
        mRangeValue.setText(text);
    }
    
    public void onPopupMenuInflated(Menu popupMenu)
    {
        // do nothing
    }
    
    public boolean onPopupMenuItemClicked(MenuItem item)
    {
        return false;
    }
}
