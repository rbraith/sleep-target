package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import java.util.List;

public class MoodDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private OnClickListener mNegativeListener;
    private OnClickListener mPositiveListener;
    private int mNegativeTextId;
    private int mPositiveTextId;
    private List<MoodUiData> mMoods;
    private MoodViewFactory mMoodViewFactory;
    private int mSelectedMoodIndex = NO_MOOD_SELECTED;
    private SelectionData mSelected;
    private Highlighter mHighlighter = new Highlighter();

//*********************************************************
// private constants
//*********************************************************

    private static final String MOOD_VIEW_TAG_PREFIX = "MoodSelectorDialog_Mood";

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_MOOD_SELECTED = -1;

//*********************************************************
// public helpers
//*********************************************************

    public interface OnClickListener
    {
        void onClick(SelectionData selection);
    }
    
    public static class Builder
    {
        private OnClickListener mNegativeListener;
        private OnClickListener mPositiveListener;
        private int mNegativeTextId;
        private int mPositiveTextId;
        private List<MoodUiData> mMoods;
        private int mSelectedMoodIndex = MoodDialogFragment.NO_MOOD_SELECTED;
        private final MoodViewFactory mMoodViewFactory;
        
        public Builder(List<MoodUiData> moods, MoodViewFactory moodViewFactory)
        {
            mMoods = moods;
            mMoodViewFactory = moodViewFactory;
        }
        
        public Builder setSelectedMood(int moodIndex)
        {
            mSelectedMoodIndex = moodIndex;
            return this;
        }
        
        public Builder setNegativeButton(int textId, OnClickListener negativeListener)
        {
            mNegativeTextId = textId;
            mNegativeListener = negativeListener;
            return this;
        }
        
        public Builder setPositiveButton(int textId, OnClickListener positiveListener)
        {
            mPositiveTextId = textId;
            mPositiveListener = positiveListener;
            return this;
        }
        
        public MoodDialogFragment build()
        {
            MoodDialogFragment dialog = new MoodDialogFragment();
            dialog.mPositiveTextId = mPositiveTextId;
            dialog.mPositiveListener = mPositiveListener;
            dialog.mNegativeTextId = mNegativeTextId;
            dialog.mNegativeListener = mNegativeListener;
            dialog.mMoods = mMoods;
            dialog.mSelectedMoodIndex = mSelectedMoodIndex;
            dialog.mMoodViewFactory = mMoodViewFactory;
            return dialog;
        }
    }
    
    public static class SelectionData
    {
        public MoodUiData mood;
        public int index;
        
        public SelectionData(MoodUiData mood, int index)
        {
            this.mood = mood;
            this.index = index;
        }
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(createMoodEditDialogView(mMoods));
        
        if (mPositiveListener != null) {
            builder.setPositiveButton(mPositiveTextId,
                                      (dialog, which) -> mPositiveListener.onClick(mSelected));
        }
        
        if (mNegativeListener != null) {
            builder.setNegativeButton(mNegativeTextId,
                                      (dialog, which) -> mNegativeListener.onClick(mSelected));
        }
        
        return builder.create();
    }

//*********************************************************
// api
//*********************************************************

    public static String formatMoodTag(int moodIndex)
    {
        return MOOD_VIEW_TAG_PREFIX + moodIndex;
    }



//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Creates a matrix of mood views.
     */
    private View createMoodEditDialogView(List<MoodUiData> moods)
    {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        
        int rowWidth = 6;
        for (int i = 0; i < moods.size(); i += rowWidth) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            
            for (int j = i; j < i + rowWidth; j++) {
                row.addView(createMoodView(moods.get(j), j));
            }
            
            layout.addView(row);
        }
        
        return layout;
    }
    
    private View createMoodView(final MoodUiData moodData, final int moodIndex)
    {
        View moodView = mMoodViewFactory.createView(moodData, requireContext(), 24f);
        // The tag mainly facilitates testing
        moodView.setTag(formatMoodTag(moodIndex));
        
        if (mSelectedMoodIndex == moodIndex) {
            mHighlighter.highlight(moodView);
        }
        
        moodView.setOnClickListener(v -> {
            mHighlighter.highlight(v);
            setSelected(new SelectionData(moodData, moodIndex));
        });
        
        return moodView;
    }
    
    private void setSelected(SelectionData selected)
    {
        mSelected = selected;
    }

//*********************************************************
// private helpers
//*********************************************************

    private static class Highlighter
    {
        private View mView;
        
        public void highlight(View v)
        {
            unhighlight();
            mView = v;
            mView.setBackgroundColor(Color.CYAN);
        }
        
        public void unhighlight()
        {
            if (mView != null) {
                mView.setBackgroundColor(Color.TRANSPARENT);
                mView = null;
            }
        }
    }
}
