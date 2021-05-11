package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

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
    private TagScrollController mTagScrollController;
    private Button mAddTagsButton;
    
    private TagSelectorViewModel mViewModel;
    
    private Context mContext;
    private FragmentManager mFragmentManager;
    
    private View.OnClickListener mDisplayDialogListener = v -> displayTagDialog();

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_TAG = "TagSelectorController_dialog";

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
        mTagScrollController = new TagScrollController(mSelectedTagsScrollView);
        mTagScrollController.setOnClickListener(mDisplayDialogListener);
        
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
        mTagScrollController.setTags(selectedTags);
    }
}
