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
package com.rbraithwaite.sleeptarget.ui.post_sleep;

import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment2;
import com.rbraithwaite.sleeptarget.ui.common.views.ActionFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.SelectedTagAdapter;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFragmentDirections;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import java.io.Serializable;

// REFACTOR [21-08-17 2:39AM] -- This duplicates a lot of the update mode of DetailsFragment.
public class PostSleepFragment
        extends ActionFragment<PostSleepViewModel>
{
//*********************************************************
// private constants
//*********************************************************

    private static final int ICON_OPTION_DELETE = R.drawable.ic_baseline_delete_forever_24;
    private static final String POST_SLEEP_DISCARD_DIALOG = "PostSleepDiscardDialog";

//*********************************************************
// public helpers
//*********************************************************

    public static class Args
            implements Serializable
    {
        public StoppedSessionData stoppedSessionData;
        public static final long serialVersionUID = 20210112L;
        
        public Args(StoppedSessionData stoppedSessionData)
        {
            this.stoppedSessionData = stoppedSessionData;
        }
    }
    
    public static class DiscardDialog
            extends AlertDialogFragment2
    {
        public static class Actions
                extends AlertDialogFragment2.Actions {}
        
        @Override
        protected AlertDialog createAlertDialog()
        {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(requireContext());
            
            Actions actions = getActions(Actions.class);
            
            builder.setTitle(R.string.postsleep_discard_title)
                    .setMessage(R.string.postsleep_discard_warning)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.discard,
                                       ((dialog, which) -> actions.positiveAction()));
            
            return builder.create();
        }
    }

//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.post_sleep_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        handleAction();
        
        PostSleepViewModel viewModel = getViewModel();
        
        viewModel.init(getArgs().stoppedSessionData);
        
        TextView startText = view.findViewById(R.id.postsleep_start_value);
        startText.setText(viewModel.getStartText());
        
        TextView endText = view.findViewById(R.id.postsleep_stop_value);
        endText.setText(viewModel.getEndText());
        
        TextView durationText = view.findViewById(R.id.postsleep_duration);
        durationText.setText(viewModel.getDurationText());
        
        initMood(view);
        initTags(view);
        initAdditionalComments(view);
        initInterruptions(view);
        initRating(view);
        
        // discard dialog callback
        getActivityViewModel(DiscardDialog.Actions.class).onPositiveAction().observe(
                getViewLifecycleOwner(),
                event -> {
                    if (event.isFresh()) {
                        getViewModel().onDiscardConfirmed();
                    }
                });
    }
    
    @Override
    protected boolean onPositiveAction()
    {
        getViewModel().setAction(PostSleepViewModel.KEEP);
        navigateUp();
        return true;
    }
    
    @Override
    protected boolean onNegativeAction()
    {
        displayDiscardDialog();
        return true;
    }
    
    @Override
    protected Params getActionFragmentParams()
    {
        Params params = new Params();
        params.negativeIcon = ICON_OPTION_DELETE;
        return params;
    }
    
    @Override
    protected Properties<PostSleepViewModel> initProperties()
    {
        return new Properties<>(false, PostSleepViewModel.class);
    }

//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(Args args)
    {
        return SleepTrackerFragmentDirections.actionSleeptrackerToPostsleep(args).getArguments();
    }
    
    public RecyclerView getInterruptionsRecycler()
    {
        return getView().findViewById(R.id.interruptions_recycler);
    }

//*********************************************************
// private methods
//*********************************************************

    private Args getArgs()
    {
        PostSleepFragmentArgs safeArgs = PostSleepFragmentArgs.fromBundle(getArguments());
        return safeArgs.getArgs();
    }
    
    private void initMood(View fragmentRoot)
    {
        MoodUiData mood = getViewModel().getMood();
        
        FrameLayout moodFrame = fragmentRoot.findViewById(R.id.postsleep_mood_frame);
        
        if (mood != null) {
            moodFrame.addView(createMoodView(mood));
        } else {
            TextView noMoodMessage = new TextView(
                    fragmentRoot.getContext(),
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
    
    private void initTags(View fragmentRoot)
    {
        RecyclerView tagsRecycler = fragmentRoot.findViewById(R.id.postsleep_tags_recycler);
        
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(fragmentRoot.getContext());
        tagsRecycler.setLayoutManager(layoutManager);
        
        SelectedTagAdapter adapter = new SelectedTagAdapter();
        tagsRecycler.setAdapter(adapter);
        
        LiveDataFuture.getValue(
                getViewModel().getTags(),
                getViewLifecycleOwner(),
                adapter::setSelectedTags);
    }
    
    private void initAdditionalComments(View fragmentRoot)
    {
        TextView commentsText = fragmentRoot.findViewById(R.id.postsleep_comments_value);
        String additionalComments = getViewModel().getAdditionalComments();
        // REFACTOR [21-05-9 3:19PM] -- this text & color stuff is state that should probably be
        //  handled in the view model.
        if (additionalComments == null || additionalComments.equals("")) {
            // display 'No Comments' message instead
            commentsText.setVisibility(View.GONE);
            TextView noCommentsMessage = new TextView(
                    fragmentRoot.getContext(),
                    null,
                    R.attr.trackerPostDialogNullDataMessageStyle);
            noCommentsMessage.setText(R.string.postsleepdialog_nocomments);
            
            NestedScrollView commentsScroll =
                    fragmentRoot.findViewById(R.id.postsleep_comments_scroll);
            commentsScroll.removeAllViews();
            commentsScroll.addView(noCommentsMessage);
        } else {
            commentsText.setText(additionalComments);
        }
    }
    
    private void initInterruptions(View fragmentRoot)
    {
        FrameLayout interruptionsContent =
                fragmentRoot.findViewById(R.id.post_sleep_interruptions_content);
        
        if (getViewModel().hasNoInterruptions()) {
            getLayoutInflater().inflate(R.layout.post_sleep_interruptions_nodata,
                                        interruptionsContent);
        } else {
            initInterruptionsDataDisplay(interruptionsContent);
        }
    }
    
    private void initInterruptionsDataDisplay(FrameLayout parent)
    {
        View interruptionsLayout =
                getLayoutInflater().inflate(R.layout.common_interruptions, parent);
        
        TextView countText = interruptionsLayout.findViewById(R.id.interruptions_count);
        countText.setText(getViewModel().getInterruptionsCountText());
        
        TextView totalTimeText =
                interruptionsLayout.findViewById(R.id.interruptions_total);
        totalTimeText.setText(getViewModel().getInterruptionsTotalTimeText());
        
        RecyclerView interruptionsRecycler =
                interruptionsLayout.findViewById(R.id.interruptions_recycler);
        interruptionsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        interruptionsRecycler.setAdapter(new PostSleepInterruptionsAdapter(getViewModel().getInterruptionsListItems()));
    }
    
    private void initRating(View fragmentRoot)
    {
        RatingBar ratingBar = fragmentRoot.findViewById(R.id.postsleep_star_rating);
        ratingBar.setRating(getViewModel().getRating());
        
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            getViewModel().setRating(rating);
        });
    }
    
    private void displayDiscardDialog()
    {
        new DiscardDialog().show(getChildFragmentManager(), POST_SLEEP_DISCARD_DIALOG);
    }
    
    private void handleAction()
    {
        getViewModel().getAction().observe(
                getViewLifecycleOwner(),
                action -> {
                    if (action == PostSleepViewModel.DISCARD) {
                        navigateUp();
                    }
                });
    }
}
