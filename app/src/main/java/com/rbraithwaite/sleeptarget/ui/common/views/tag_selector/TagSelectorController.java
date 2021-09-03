/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.ui.common.views.tag_selector;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleeptarget.R;

// REFACTOR [21-04-8 11:08PM] -- maybe I should call this TagManager instead? (or at least that's
//  what the dialog does).
public class TagSelectorController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private RecyclerView mSelectedRecycler;
    private SelectedTagAdapter mSelectedTagAdapter;
    private Button mAddTagsButton;
    
    private TagSelectorViewModel mViewModel;
    
    private Context mContext;
    private FragmentManager mFragmentManager;

//*********************************************************
// public constants
//*********************************************************

    public static final String DIALOG_TAG = "TagSelectorController_dialog";

//*********************************************************
// constructors
//*********************************************************

    public TagSelectorController(
            View root,
            TagSelectorViewModel viewModel,
            LifecycleOwner lifecycleOwner,
            FragmentManager fragmentManager)
    {
        mRoot = root;
        mSelectedRecycler = root.findViewById(R.id.tag_selector_selected_recycler);
        mAddTagsButton = root.findViewById(R.id.tag_selector_add_tags_btn);
        
        mContext = mRoot.getContext();
        mFragmentManager = fragmentManager;
        mViewModel = viewModel;
        
        mAddTagsButton.setOnClickListener(v -> displayTagDialog());
        
        initSelectedRecycler();
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
                        mSelectedRecycler.setVisibility(View.GONE);
                        mAddTagsButton.setVisibility(View.VISIBLE);
                    } else {
                        mSelectedRecycler.setVisibility(View.VISIBLE);
                        mAddTagsButton.setVisibility(View.GONE);
                        
                        mSelectedTagAdapter.setSelectedTags(selectedTags);
                    }
                });
    }
    
    private void initSelectedRecycler()
    {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
        mSelectedRecycler.setLayoutManager(layoutManager);
        
        mSelectedTagAdapter = new SelectedTagAdapter();
        mSelectedTagAdapter.setOnItemClickedListener(vh -> displayTagDialog());
        mSelectedRecycler.setAdapter(mSelectedTagAdapter);
    }
    
    private void displayTagDialog()
    {
        // REFACTOR [21-05-26 9:21PM] -- abstract this stuff, I guess getDialogThemeId().
        TypedArray ta = mRoot.getContext().obtainStyledAttributes(R.styleable.TagSelectorComponent);
        int dialogThemeId =
                ta.getResourceId(R.styleable.TagSelectorComponent_tagSelectorDialogTheme, -1);
        ta.recycle();
        
        mViewModel.setDialogThemeId(dialogThemeId);
        TagSelectorDialogFragment
                .createInstance()
                .show(mFragmentManager, DIALOG_TAG);
    }
}
