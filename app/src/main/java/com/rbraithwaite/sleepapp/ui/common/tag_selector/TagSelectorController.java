package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;

import java.util.List;

// REFACTOR [21-04-8 11:08PM] -- maybe I should call this TagManager instead? (or at least that's
//  what the dialog does).
public class TagSelectorController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private ScrollView mSelectedTagsScrollView;
    private Button mAddTagsButton;
    
    private TagSelectorViewModel mViewModel;
    
    private Context mContext;
    private FragmentManager mFragmentManager;
    
    private View.OnClickListener mDisplayDialogListener = v -> displayTagDialog();
    private LinearLayout.LayoutParams mTagViewParams;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_TAG = "TagSelectorController_dialog";
    private final int DEFAULT_VIEW_SPACING = 15;
    private final int MAX_ROW_CHARACTER_LENGTH = 20;

//*********************************************************
// public constants
//*********************************************************

    public static final String SELECTED_TAGS_TAG = "TagSelectorController_selected";
    
//*********************************************************
// constructors
//*********************************************************

    public TagSelectorController(
            View root,
            TagSelectorViewModel viewModel,
            LifecycleOwner lifecycleOwner,
            final Context context,
            FragmentManager fragmentManager)
    {
        mRoot = root;
        
        mSelectedTagsScrollView = root.findViewById(R.id.tag_selector_tags_scroll);
        mAddTagsButton = root.findViewById(R.id.tag_selector_add_tags_btn);
        
        mContext = context;
        mFragmentManager = fragmentManager;
        mViewModel = viewModel;
        
        mAddTagsButton.setOnClickListener(mDisplayDialogListener);
        
        bindViewModel(lifecycleOwner);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void bindViewModel(LifecycleOwner lifecycleOwner)
    {
        mViewModel.getSelectedTags().observe(
                lifecycleOwner,
                selectedTags -> {
                    if (selectedTags.isEmpty()) {
                        mSelectedTagsScrollView.setVisibility(View.INVISIBLE);
                        mAddTagsButton.setVisibility(View.VISIBLE);
                    } else {
                        mSelectedTagsScrollView.setVisibility(View.VISIBLE);
                        mAddTagsButton.setVisibility(View.GONE);
                        
                        updateSelectedTagsScrollView(selectedTags);
                    }
                });
    }
    
    private void displayTagDialog()
    {
        TagSelectorDialogFragment
                .createInstance(mViewModel)
                .show(mFragmentManager, DIALOG_TAG);
    }
    
    /**
     * This is assumed to only be called if selectedTags has > 0 elems.
     */
    private void updateSelectedTagsScrollView(List<TagUiData> selectedTags)
    {
        mSelectedTagsScrollView.removeAllViews();
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, DEFAULT_VIEW_SPACING, 0, 0);
        
        // OPTIMIZE [21-04-7 11:52PM] -- the root vertical layout could be in the xml - it doesn't
        //  need to be recreated every time.
        LinearLayout layout = createLinearLayout(mContext, layoutParams, LinearLayout.VERTICAL);
        layout.setOnClickListener(mDisplayDialogListener);
        layout.setTag(SELECTED_TAGS_TAG);
        
        LinearLayout tagRow = createLinearLayout(mContext, rowParams, LinearLayout.HORIZONTAL);
        int rowCharacterLength = 0;
        for (TagUiData tag : selectedTags) {
            rowCharacterLength += tag.text.length();
            
            // new rows are decided by the amount of characters in the tags on each row, like a
            // new line in a text document
            if (rowCharacterLength > MAX_ROW_CHARACTER_LENGTH) {
                // this row is done, start a new row
                layout.addView(tagRow);
                tagRow = createLinearLayout(mContext, rowParams, LinearLayout.HORIZONTAL);
                rowCharacterLength = tag.text.length();
            }
            
            tagRow.addView(generateTagView(tag));
        }
        // the last row will still need to be added
        layout.addView(tagRow);
        
        mSelectedTagsScrollView.addView(layout);
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
}
