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

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodDialogRecyclerAdapter;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.Constants;

import java.io.Serializable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MoodDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private static int NO_THEME = -1;
    
    
    private State mState = new State();

//*********************************************************
// private constants
//*********************************************************

    private static final String MOOD_VIEW_TAG_PREFIX = "MoodSelectorDialog_Mood";
    
    private static final String STATE_KEY = "state";

//*********************************************************
// private helpers
//*********************************************************
    
    private static class State implements Serializable
    {
        public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;
        
        public int themeId = NO_THEME;
        
        public MoodUiData selectedMood = new MoodUiData();
        
        public Mode mode;
    }
    
//*********************************************************
// public constants
//*********************************************************

    public static final String RECYCLER_TAG = "MoodDialogRecycler";

//*********************************************************
// public helpers
//*********************************************************
    
    public enum Mode
    {
        ADD, EDIT
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        maybeInitFromSavedInstanceState(savedInstanceState);
        
        AlertDialog.Builder builder = (mState.themeId == NO_THEME) ?
                new AlertDialog.Builder(requireContext()) :
                new AlertDialog.Builder(requireContext(), mState.themeId);
        
        builder.setView(createMoodGridView());
        // REFACTOR [21-06-12 9:07PM] -- hardcoded string.
        builder.setTitle("Select A Mood:");
        
        builder.setPositiveButton(
                R.string.confirm,
                (dialog, which) -> onPositiveClick());
        
        builder.setNegativeButton(
                mState.mode == Mode.ADD ? R.string.cancel : R.string.delete,
                (dialog, which) -> onNegativeClick());
        
        return builder.create();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        outState.putSerializable(STATE_KEY, mState);
        super.onSaveInstanceState(outState);
    }
    
//*********************************************************
// api
//*********************************************************
    
    public void setMode(Mode mode)
    {
        mState.mode = mode;
    }

    public static MoodDialogFragment createInstance(int themeId)
    {
        MoodDialogFragment fragment = new MoodDialogFragment();
        fragment.mState.themeId = themeId;
        return fragment;
    }
    
    public static MoodDialogFragment createInstance()
    {
        return createInstance(NO_THEME);
    }
    
    public static String formatMoodTag(int moodIndex)
    {
        return MOOD_VIEW_TAG_PREFIX + moodIndex;
    }
    
    public void setSelectedMood(MoodUiData selectedMood)
    {
        mState.selectedMood = selectedMood;
    }


//*********************************************************
// private methods
//*********************************************************
    
    private void onPositiveClick()
    {
        // SMELL [21-10-15 11:05PM] -- This is making a strong assumption that the view model
        //  passed to the MoodSelectorController is the activity-stored one - I should enforce
        //  this or something.
        MoodSelectorViewModel viewModel = MoodSelectorViewModel.getInstanceFrom(requireActivity());
        viewModel.setMood(mState.selectedMood);
    }
    
    private void onNegativeClick()
    {
        // don't do anything if mode is ADD
        if (mState.mode == Mode.EDIT) {
            MoodSelectorViewModel viewModel = MoodSelectorViewModel.getInstanceFrom(requireActivity());
            viewModel.clearSelectedMood();
        }
    }

    private void maybeInitFromSavedInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable(STATE_KEY);
        }
    }
    
    /**
     * Creates a matrix of mood views.
     */
    private View createMoodGridView()
    {
        RecyclerView recyclerView = (mState.themeId == NO_THEME) ?
                new RecyclerView(requireContext()) :
                new RecyclerView(new ContextThemeWrapper(requireContext(), mState.themeId));
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        recyclerView.setTag(RECYCLER_TAG); // this is mainly for tests
        UiUtils.initViewMarginLayoutParams(
                recyclerView,
                new UiUtils.SizeDp(MATCH_PARENT, MATCH_PARENT),
                new UiUtils.MarginsDp(32, 32, 32, 0));
        
        MoodDialogRecyclerAdapter moodRecyclerAdapter = new MoodDialogRecyclerAdapter();
        moodRecyclerAdapter.setOnSelectionChangedListener(selectedMoodIndex -> setSelectedMood(
                new MoodUiData(selectedMoodIndex)));
        
        // REFACTOR [21-06-25 5:36PM] -- Since Mood and MoodUiData can be in an unset state, there's
        //  no reason to ever be passing around null Mood or MoodUiData instances - fix all
        //  occurrences of this.
        if (mState.selectedMood == null) {
            moodRecyclerAdapter.setSelectedMoodPosition(null);
        } else {
            moodRecyclerAdapter.setSelectedMoodPosition(mState.selectedMood.asIndex());
        }
        
        recyclerView.setAdapter(moodRecyclerAdapter);
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.addView(recyclerView);
        
        return layout;
    }
}
