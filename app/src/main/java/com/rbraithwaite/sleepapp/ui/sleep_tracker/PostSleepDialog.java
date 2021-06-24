package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.utils.UiUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.util.List;

// REFACTOR [21-04-30 4:02PM] -- there is a common pattern between this, TagSelectorDialogFragment,
//  and MoodDialogFragment. All are dialogs acting as bare frames for custom layouts - each needs
//  the
//  layout view, a controller (the fragment itself), and a view model. Consider making some general
//  ViewDialogFragment or something. (How does AlertDialogFragment play into this - my thinking
//  was it wouldn't make sense to derive these from AlertDialogFragment since it exposes the
//  dialog creation (this would force clients of the dialog fragment to act as its controller -
//  not ideal for complex views))
//  ---
//  Also this makes me think I should redesign how the tag selector system works - right now, all
//  the controller logic is in the recycler adapter. Maybe this logic should be in the dialog
//  fragment, and have the adapter be bare-bones & using simple data? (So eg the dialog fragment
//  would bind to the view model, and update the adapter from there)
public class PostSleepDialog
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private OnKeepSessionListener mOnKeepSessionListener;
    private OnDiscardSessionListener mOnDiscardSessionListener;
    private PostSleepDialogViewModel mViewModel;
    private TagScrollController mTagScrollController;

//*********************************************************
// package properties
//*********************************************************

    ConstraintLayout mRoot;
    RatingBar mRatingBar;

//*********************************************************
// public helpers
//*********************************************************

    public interface OnKeepSessionListener
    {
        void onKeepSession(PostSleepData postSleepData);
    }
    
    public interface OnDiscardSessionListener
    {
        void onDiscardSession();
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        mRoot = (ConstraintLayout) getLayoutInflater().inflate(R.layout.post_sleep_dialog, null);
        
        mRatingBar = mRoot.findViewById(R.id.postsleep_star_rating);
        mRatingBar.setRating(mViewModel.getRating());
        
        initSessionDetails(mRoot);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(mRoot)
                .setTitle(R.string.postsleepdialog_title)
                .setPositiveButton(R.string.keep, (dialog, which) -> {
                    if (mOnKeepSessionListener != null) {
                        // REFACTOR [21-04-30 6:02PM] [big job]
                        //  I need to be using LiveData better - it's actually totally fine to
                        //  use getValue() - this
                        //  is just a sort of instant snapshot of where this LiveData is at right
                        //  now (I just need
                        //  to handle cases where the value is null because the live data is
                        //  unset or whatever)
                        //  ---
                        //  Some places *need* the LiveDataFuture, as they are waiting on async
                        //  work to init the value.
                        //  There are other places though where its totally fine to use getValue
                        //  for the current value.
                        mOnKeepSessionListener.onKeepSession(mViewModel.getPostSleepData()
                                                                     .getValue());
                    }
                })
                .setNegativeButton(R.string.discard, (dialog, which) -> {
                    if (mOnDiscardSessionListener != null) {
                        mOnDiscardSessionListener.onDiscardSession();
                    }
                });
        
        mRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            mViewModel.setRating(rating);
        });
        
        AlertDialog alertDialog = builder.create();
        // This dialog can't be cancelled by clicking outside since it is a kind of "point of no
        // return" - the user must now either record the session to the archive or discard it.
        alertDialog.setCanceledOnTouchOutside(false);
        
        return alertDialog;
    }

//*********************************************************
// api
//*********************************************************

    public static PostSleepDialog createInstance(
            PostSleepDialogViewModel viewModel,
            OnKeepSessionListener onKeepSessionListener,
            OnDiscardSessionListener onDiscardSessionListener)
    {
        PostSleepDialog dialog = new PostSleepDialog();
        dialog.mViewModel = viewModel;
        dialog.mOnKeepSessionListener = onKeepSessionListener;
        dialog.mOnDiscardSessionListener = onDiscardSessionListener;
        return dialog;
    }
    
    public int getMoodIndex()
    {
        MoodUiData mood = mViewModel.getMood();
        return mood.asIndex();
    }
    
    public List<TagUiData> getTags()
    {
        return mTagScrollController.getTags();
    }
    
    public PostSleepDialogViewModel getViewModel()
    {
        return mViewModel;
    }
    
    public TagScrollController getTagScrollController()
    {
        return mTagScrollController;
    }

//*********************************************************
// private methods
//*********************************************************

    private void initSessionDetails(View dialogRoot)
    {
        TextView startText = dialogRoot.findViewById(R.id.postsleep_start_value);
        startText.setText(mViewModel.getStartText());
        
        TextView endText = dialogRoot.findViewById(R.id.postsleep_stop_value);
        endText.setText(mViewModel.getEndText());
        
        TextView durationText = dialogRoot.findViewById(R.id.postsleep_duration);
        durationText.setText(mViewModel.getDurationText());
        
        initMood(dialogRoot);
        
        initTags(dialogRoot);
        
        initAdditionalComments(dialogRoot);
    }
    
    private void initAdditionalComments(View dialogRoot)
    {
        TextView commentsText = dialogRoot.findViewById(R.id.postsleep_comments_value);
        String additionalComments = mViewModel.getAdditionalComments();
        // REFACTOR [21-05-9 3:19PM] -- this text & color stuff is state that should probably be
        //  handled in the view model.
        if (additionalComments == null || additionalComments.equals("")) {
            // display 'No Comments' message instead
            commentsText.setVisibility(View.GONE);
            TextView noCommentsMessage = new TextView(
                    mRoot.getContext(),
                    null,
                    R.attr.trackerPostDialogNullDataMessageStyle);
            noCommentsMessage.setText(R.string.postsleepdialog_nocomments);
            
            ScrollView commentsScroll = dialogRoot.findViewById(R.id.postsleep_comments_scroll);
            commentsScroll.removeAllViews();
            commentsScroll.addView(noCommentsMessage);
        } else {
            commentsText.setText(additionalComments);
        }
    }
    
    private void initMood(View dialogRoot)
    {
        MoodUiData mood = mViewModel.getMood();
        
        FrameLayout moodFrame = dialogRoot.findViewById(R.id.postsleep_mood_frame);
        
        if (mood != null) {
            moodFrame.addView(createMoodView(mood));
        } else {
            TextView noMoodMessage = new TextView(
                    mRoot.getContext(),
                    null,
                    R.attr.trackerPostDialogNullDataMessageStyle);
            noMoodMessage.setText(R.string.postsleepdialog_nomood);
            moodFrame.addView(noMoodMessage);
        }
    }
    
    private MoodView createMoodView(MoodUiData mood)
    {
        MoodView moodView = new MoodView(requireContext());
        moodView.setMood(mood.asIndex());
        moodView.setMoodColor(getMoodColor());
        // REFACTOR [21-06-13 2:29AM] -- hardcoded size.
        UiUtils.initViewMarginLayoutParams(moodView, new UiUtils.SizeDp(40));
        return moodView;
    }
    
    private int getMoodColor()
    {
        // REFACTOR [21-06-13 2:23AM] -- This color is shared w/ the mood selector.
        TypedArray ta = requireContext().obtainStyledAttributes(new int[] {R.attr.colorSecondary});
        try {
            return ta.getColor(0, -1);
        } finally {
            ta.recycle();
        }
    }
    
    private void initTags(View dialogRoot)
    {
        mTagScrollController = new TagScrollController(
                dialogRoot.findViewById(R.id.postsleep_tags_scroll));
        
        LiveDataFuture.getValue(
                mViewModel.getTags(),
                this,
                mTagScrollController::setTags);
    }
}
