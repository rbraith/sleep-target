package com.rbraithwaite.sleepapp.ui.common.mood_selector;

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

import com.rbraithwaite.sleepapp.ui.UiUtils;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.TEMP.MoodDialogRecyclerAdapter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MoodDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private static int NO_THEME = -1;
    private OnClickListener mNegativeListener;
    private OnClickListener mPositiveListener;
    private int mNegativeTextId;
    private int mPositiveTextId;
    
    private MoodUiData mSelectedMood = new MoodUiData();
    
    
    private int mThemeId = NO_THEME;
    
//*********************************************************
// private constants
//*********************************************************

    private static final String MOOD_VIEW_TAG_PREFIX = "MoodSelectorDialog_Mood";

//*********************************************************
// public constants
//*********************************************************

    public static final String RECYCLER_TAG = "MoodDialogRecycler";
    

//*********************************************************
// public helpers
//*********************************************************

    public interface OnClickListener
    {
        void onClick(MoodUiData selectedMood);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = (mThemeId == NO_THEME) ?
                new AlertDialog.Builder(requireContext()) :
                new AlertDialog.Builder(requireContext(), mThemeId);
        
        builder.setView(createMoodGridView());
        // REFACTOR [21-06-12 9:07PM] -- hardcoded string.
        builder.setTitle("Select A Mood:");
        
        if (mPositiveListener != null) {
            builder.setPositiveButton(mPositiveTextId,
                                      (dialog, which) -> mPositiveListener.onClick(mSelectedMood));
        }
        
        if (mNegativeListener != null) {
            builder.setNegativeButton(mNegativeTextId,
                                      (dialog, which) -> mNegativeListener.onClick(mSelectedMood));
        }
        
        return builder.create();
    }
    
//*********************************************************
// api
//*********************************************************

    public static MoodDialogFragment createInstance(int themeId)
    {
        MoodDialogFragment fragment = new MoodDialogFragment();
        fragment.mThemeId = themeId;
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
    
    public void setNegativeButton(int textId, OnClickListener negativeListener)
    {
        mNegativeTextId = textId;
        mNegativeListener = negativeListener;
    }
    
    public void setPositiveButton(int textId, OnClickListener positiveListener)
    {
        mPositiveTextId = textId;
        mPositiveListener = positiveListener;
    }


    public void setSelectedMood(MoodUiData selectedMood)
    {
        mSelectedMood = selectedMood;
    }

//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Creates a matrix of mood views.
     */
    private View createMoodGridView()
    {
        RecyclerView recyclerView = (mThemeId == NO_THEME) ?
                new RecyclerView(requireContext()) :
                new RecyclerView(new ContextThemeWrapper(requireContext(), mThemeId));
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        recyclerView.setTag(RECYCLER_TAG); // this is mainly for tests
        UiUtils.initViewMarginLayoutParams(
                recyclerView,
                new UiUtils.SizeDp(MATCH_PARENT, MATCH_PARENT),
                new UiUtils.MarginsDp(32, 32, 32, 0));
        
        MoodDialogRecyclerAdapter moodRecyclerAdapter = new MoodDialogRecyclerAdapter();
        moodRecyclerAdapter.setOnSelectionChangedListener(selectedMoodIndex -> setSelectedMood(
                new MoodUiData(selectedMoodIndex)));
        moodRecyclerAdapter.setSelectedMoodPosition(mSelectedMood.asIndex());
        
        recyclerView.setAdapter(moodRecyclerAdapter);
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.addView(recyclerView);
        
        return layout;
    }
}
