/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.TagSelectorBinding;

public class TagSelectorComponent
        extends ConstraintLayout
{
//*********************************************************
// private properties
//*********************************************************

    private TagSelectorBinding mBinding;
    
    private SelectedTagAdapter mSelectedTagAdapter;
    private TagSelectorViewModel mViewModel;
    private FragmentManager mFragmentManager;
    
//*********************************************************
// public constants
//*********************************************************

    public static final String DIALOG_TAG = "TagSelectorController_dialog";
    
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
// api
//*********************************************************

    public void init(
            TagSelectorViewModel viewModel,
            LifecycleOwner lifecycleOwner,
            FragmentManager fragmentManager)
    {
        mFragmentManager = fragmentManager;
        mViewModel = viewModel;
        
        mBinding.tsAddTagsButton.setOnClickListener(v -> displayTagDialog());
        
        initSelectedRecycler();
        bindViewModel(lifecycleOwner);
    }
    
//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-05-25 1:38PM] -- This pattern is repeated in MoodSelectorComponent
    //  consider making a ConstraintLayoutComponent base class.
    private void initComponent(Context context)
    {
        inflate(context, R.layout.tag_selector, this);
        mBinding = TagSelectorBinding.bind(this);
    }
    
    private void bindViewModel(LifecycleOwner lifecycleOwner)
    {
        mViewModel.getSelectedTags().observe(
                lifecycleOwner,
                selectedTags -> {
                    if (selectedTags.isEmpty()) {
                        mBinding.tsSelectedRecycler.setVisibility(View.GONE);
                        mBinding.tsAddTagsButton.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.tsSelectedRecycler.setVisibility(View.VISIBLE);
                        mBinding.tsAddTagsButton.setVisibility(View.GONE);
                        
                        mSelectedTagAdapter.setSelectedTags(selectedTags);
                    }
                });
    }
    
    private void initSelectedRecycler()
    {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        mBinding.tsSelectedRecycler.setLayoutManager(layoutManager);
        
        mSelectedTagAdapter = new SelectedTagAdapter();
        mSelectedTagAdapter.setOnItemClickedListener(vh -> displayTagDialog());
        mBinding.tsSelectedRecycler.setAdapter(mSelectedTagAdapter);
    }
    
    private void displayTagDialog()
    {
        // REFACTOR [21-05-26 9:21PM] -- abstract this stuff, I guess getDialogThemeId().
        TypedArray ta = getContext().obtainStyledAttributes(R.styleable.TagSelectorComponent);
        int dialogThemeId =
                ta.getResourceId(R.styleable.TagSelectorComponent_tagSelectorDialogTheme, -1);
        ta.recycle();
        
        mViewModel.setDialogThemeId(dialogThemeId);
        TagSelectorDialogFragment
                .createInstance()
                .show(mFragmentManager, DIALOG_TAG);
    }
}
