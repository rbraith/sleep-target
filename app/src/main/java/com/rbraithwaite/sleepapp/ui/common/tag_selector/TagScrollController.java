package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rbraithwaite.sleepapp.R;

import java.util.List;

public class TagScrollController
{
//*********************************************************
// private properties
//*********************************************************

    private List<TagUiData> mTags;
    
    private ScrollView mRootScroll;
    private LinearLayout mLayout;
    
    private Context mContext;
    private LinearLayout.LayoutParams mTagViewParams;

//*********************************************************
// private constants
//*********************************************************

    private final int DEFAULT_VIEW_SPACING = 15;
    private final int MAX_ROW_CHARACTER_LENGTH = 20;

//*********************************************************
// public constants
//*********************************************************

    public static final String TAGS_TAG = "TagScrollController_layout";
    
//*********************************************************
// constructors
//*********************************************************

    public TagScrollController(ScrollView rootScroll)
    {
        mRootScroll = rootScroll;
        mContext = mRootScroll.getContext();
        mLayout = initLayout(mContext);
        
        mRootScroll.removeAllViews();
        mRootScroll.addView(mLayout);
    }
    
//*********************************************************
// api
//*********************************************************

    public Boolean isEmpty()
    {
        return mTags == null ||
               mTags.isEmpty();
    }
    
    public void setOnClickListener(View.OnClickListener listener)
    {
        mLayout.setOnClickListener(listener);
    }
    
    public List<TagUiData> getTags()
    {
        return mTags;
    }
    
    public void setTags(List<TagUiData> tags)
    {
        mTags = tags;
        
        if (isEmpty()) {
            displayNoTagsMessage();
        } else {
            displaysTags();
        }
    }
    
//*********************************************************
// private methods
//*********************************************************

    private LinearLayout initLayout(Context context)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        
        LinearLayout layout = createLinearLayout(context, layoutParams, LinearLayout.VERTICAL);
        layout.setTag(TAGS_TAG);
        
        return layout;
    }
    
    private void displaysTags()
    {
        mLayout.removeAllViews();
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, DEFAULT_VIEW_SPACING, 0, 0);
        
        LinearLayout tagRow = createLinearLayout(mContext, rowParams, LinearLayout.HORIZONTAL);
        int rowCharacterLength = 0;
        for (TagUiData tag : mTags) {
            rowCharacterLength += tag.text.length();
            
            // new rows are decided by the amount of characters in the tags on each row, like a
            // new line in a text document
            if (rowCharacterLength > MAX_ROW_CHARACTER_LENGTH) {
                // this row is done, start a new row
                mLayout.addView(tagRow);
                tagRow = createLinearLayout(mContext, rowParams, LinearLayout.HORIZONTAL);
                rowCharacterLength = tag.text.length();
            }
            
            tagRow.addView(generateTagView(tag));
        }
        // the last row will still need to be added
        mLayout.addView(tagRow);
    }
    
    private void displayNoTagsMessage()
    {
        mLayout.removeAllViews();
        
        TextView noTagsMessage = new TextView(mContext);
        noTagsMessage.setText(R.string.tagscroll_no_selected_tags);
        
        mLayout.addView(noTagsMessage);
    }
    
    private View generateTagView(TagUiData tag)
    {
        final int TAG_VERTICAL_PADDING = 5;
        
        TextView tagView = new TextView(mContext);
        tagView.setText(tag.text);
        tagView.setPadding(
                DEFAULT_VIEW_SPACING,
                TAG_VERTICAL_PADDING,
                DEFAULT_VIEW_SPACING,
                TAG_VERTICAL_PADDING);
        tagView.setLayoutParams(getTagViewParams());
        tagView.setBackgroundColor(Color.CYAN);
        return tagView;
    }
    
    // REFACTOR [21-04-17 10:13PM] -- maybe extract this.
    private LinearLayout createLinearLayout(
            Context context,
            LinearLayout.LayoutParams params,
            int orientation)
    {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(params);
        layout.setOrientation(orientation);
        return layout;
    }
    
    private LinearLayout.LayoutParams getTagViewParams()
    {
        // REFACTOR [21-04-14 1:17AM] -- create a generic lazy init util (using lambdas).
        if (mTagViewParams == null) {
            mTagViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mTagViewParams.setMargins(DEFAULT_VIEW_SPACING, 0, 0, 0);
        }
        return mTagViewParams;
    }
}
