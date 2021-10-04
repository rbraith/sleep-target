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
package com.rbraithwaite.sleeptarget.ui.common.views.mood_selector;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;

public class MoodSelectorController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    
    private Button mAddButton;
    private FrameLayout mMoodValue;
    
    private MoodSelectorViewModel mViewModel;
    
    private LifecycleOwner mLifecycleOwner;
    private FragmentManager mFragmentManager;
    private Context mContext;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_TAG = "MoodSelectorController_dialog";
    
    private static final int NO_ROOT_THEME_ID = -1;

//*********************************************************
// public constants
//*********************************************************

    public static final int MOOD_DISPLAY_SIZE_DP = 45;

//*********************************************************
// constructors
//*********************************************************

    // TODO [21-06-14 12:43AM] -- to make this more modular, add some custom attributes
    //  e.g.: mood value color, add button style, mood selected/deselected colors.
    public MoodSelectorController(
            View root,
            MoodSelectorViewModel viewModel,
            LifecycleOwner lifecycleOwner,
            final FragmentManager fragmentManager)
    {
        mRoot = root;
        
        mAddButton = mRoot.findViewById(R.id.mood_selector_add_btn);
        mMoodValue = mRoot.findViewById(R.id.mood_selector_mood_value);
        
        mLifecycleOwner = lifecycleOwner;
        mFragmentManager = fragmentManager;
        mContext = mRoot.getContext();
        
        mViewModel = viewModel;
        bindViewModel();
        
        mAddButton.setOnClickListener(v -> displayMoodDialog(R.string.cancel,
                                                             createOnCancelListener()));
        
        mMoodValue.setOnClickListener(v -> displayMoodDialog(R.string.delete,
                                                             createOnDeleteListener()));
    }

//*********************************************************
// private methods
//*********************************************************

    private void displayMoodDialog(
            int negativeTextId,
            MoodDialogFragment.OnClickListener negativeListener)
    {
        int rootThemeId = getRootThemeId();
        MoodDialogFragment moodDialog = rootThemeId == NO_ROOT_THEME_ID ?
                MoodDialogFragment.createInstance() :
                MoodDialogFragment.createInstance(rootThemeId);
        
        moodDialog.setPositiveButton(R.string.confirm, createOnConfirmListener());
        moodDialog.setNegativeButton(negativeTextId, negativeListener);
        moodDialog.setSelectedMood(mViewModel.getMood().getValue());
        
        moodDialog.show(mFragmentManager, DIALOG_TAG);
    }
    
    /**
     * If a theme wasn't applied to the root view, the default application theme is returned.
     */
    private int getRootThemeId()
    {
        Context context = mRoot.getContext();
        TypedArray ta = context.obtainStyledAttributes(new int[] {R.attr.theme});
        try {
            // REFACTOR [21-06-14 12:41AM] -- I ended up not needing this logic here, but it
            //  would be useful to have as a general utility somewhere I think.
//            int appThemeId = context.getApplicationInfo().theme;
            return ta.getResourceId(0, NO_ROOT_THEME_ID);
        } finally {
            ta.recycle();
        }
    }
    
    private MoodDialogFragment.OnClickListener createOnCancelListener()
    {
        return selection -> {/* do nothing */};
    }
    
    private MoodDialogFragment.OnClickListener createOnDeleteListener()
    {
        return selection -> {
            mViewModel.clearSelectedMood();
        };
    }
    
    private MoodDialogFragment.OnClickListener createOnConfirmListener()
    {
        return selection -> {
            mViewModel.setMood(selection);
        };
    }
    
    private MoodView createMoodValueView(MoodUiData moodUiData, Context context)
    {
        MoodView moodView = new MoodView(context);
        moodView.setMood(moodUiData.asIndex());
        moodView.setMoodColor(getMoodValueColor(context));
        UiUtils.initViewMarginLayoutParams(moodView, new UiUtils.SizeDp(MOOD_DISPLAY_SIZE_DP));
        return moodView;
    }
    
    private int getMoodValueColor(Context context)
    {
        // REFACTOR [21-06-14 12:08AM] -- I'm doing this in a lot of places: just grabbing one or
        //  several
        //  colour attributes
        //  I could make a ColorAttributes class
        //      eg static ColorAttributes.get(context, int attr)
        //          static ColorAttributes.get(context, int[] attrList)->int[]
        //          ColorAttributes(context)
        //              this could return an instance that caches found colours in a map
        //              returning those cached values first
        //  This class would replace AppColors.
        TypedArray ta = context.obtainStyledAttributes(new int[] {R.attr.colorSecondary});
        try {
            return ta.getColor(0, -1);
        } finally {
            ta.recycle();
        }
    }
    
    private void bindViewModel()
    {
        mViewModel.getMood().observe(mLifecycleOwner, mood -> {
            if (mood == null || !mood.isSet()) {
                mAddButton.setVisibility(View.VISIBLE);
                mMoodValue.setVisibility(View.GONE);
            } else {
                mAddButton.setVisibility(View.INVISIBLE);
                mMoodValue.setVisibility(View.VISIBLE);
                
                mMoodValue.removeAllViews();
                mMoodValue.addView(createMoodValueView(mood, mContext));
            }
        });
    }
}
