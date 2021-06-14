package com.rbraithwaite.sleepapp.ui.common.mood_selector.TEMP;

import android.content.res.TypedArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.UiUtils;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MoodDialogRecyclerAdapter
        extends RecyclerView.Adapter<MoodDialogRecyclerAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private Attributes mAttributes;
    private int mSelectedMoodPosition = NO_SELECTION;
    private OnSelectionChangedListener mOnSelectionChangedListener;

//*********************************************************
// private constants
//*********************************************************

    private final int MOOD_COUNT = MoodView.getMoodCount();

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_SELECTION = -1;
    

//*********************************************************
// public helpers
//*********************************************************

    public interface OnSelectionChangedListener
    {
        void onSelectionChanged(int selectedMoodIndex);
    }
    
    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        MoodView moodView;
        
        public ViewHolder(MoodView moodView)
        {
            super(moodView);
            this.moodView = moodView;
        }
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        maybeInitAttributes(parent);
        
        MoodView moodView = new MoodView(parent.getContext());
        
        // REFACTOR [21-06-13 11:35PM] -- I think I would prefer this abstraction to be a
        //  ViewHelper(view) class
        //  then I could do:
        //  helper = new ViewHelper(new MoodView)
        //  helper.setSize(width, height).
        //  helper.setMargins(...)
        //  helper.getView()
        //  --
        //  maybe something like:
        //      ViewWrapper<View>(view args...)
        //      then getView() can return the proper type.
        UiUtils.initViewMarginLayoutParams(
                moodView,
                // SMELL [21-06-12 8:58PM] -- This view height and the margins below are ad-hoc
                //  tuned for the current recycler grid properties in
                //  MoodDialogFragment.createMoodEditDialogView
                new UiUtils.SizeDp(MATCH_PARENT, 38),
                new UiUtils.MarginsDp(4, 0, 4, 16));
        
        return new ViewHolder(moodView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.moodView.setMood(position);
        holder.moodView.setMoodColor(position == getSelectedMoodPosition() ?
                                             mAttributes.selectedMoodColor :
                                             mAttributes.defaultMoodColor);
        holder.moodView.setOnClickListener(v -> {
            // This doesn't "toggle" the mood selection, since removing the selected mood can be
            // handled via the negative dialog button. It only switches the selection between
            // different moods.
            int oldPosition = getSelectedMoodPosition();
            if (oldPosition != position) {
                setSelectedMoodPosition(position);
                notifyItemChanged(oldPosition);
                notifyItemChanged(position);
                mOnSelectionChangedListener.onSelectionChanged(position);
            }
        });
    }
    
    @Override
    public int getItemCount()
    {
        return MOOD_COUNT;
    }
    
//*********************************************************
// api
//*********************************************************

    public int getSelectedMoodPosition()
    {
        return mSelectedMoodPosition;
    }
    
    public void setSelectedMoodPosition(Integer selectedMoodPosition)
    {
        if (selectedMoodPosition == null) {
            mSelectedMoodPosition = NO_SELECTION;
        } else {
            mSelectedMoodPosition = selectedMoodPosition;
        }
    }
    
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener)
    {
        mOnSelectionChangedListener = listener;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void maybeInitAttributes(ViewGroup parent)
    {
        mAttributes = CommonUtils.lazyInit(mAttributes, () -> {
            TypedArray ta = parent.getContext().obtainStyledAttributes(new int[] {
                    R.attr.colorPrimary, R.attr.colorSecondary
            });
            try {
                return new Attributes(
                        ta.getColor(0, -1),
                        ta.getColor(1, -1));
            } finally {
                ta.recycle();
            }
        });
    }
    
//*********************************************************
// private helpers
//*********************************************************

    private static class Attributes
    {
        public final int defaultMoodColor;
        public final int selectedMoodColor;
        
        public Attributes(int defaultMoodColor, int selectedMoodColor)
        {
            this.defaultMoodColor = defaultMoodColor;
            this.selectedMoodColor = selectedMoodColor;
        }
    }
}
