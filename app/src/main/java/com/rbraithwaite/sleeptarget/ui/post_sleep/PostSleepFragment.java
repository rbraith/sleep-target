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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.CommonInterruptionsBinding;
import com.rbraithwaite.sleeptarget.databinding.PostSleepFragmentBinding;
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
// private properties
//*********************************************************

    private PostSleepFragmentBinding mBinding;

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
        mBinding = PostSleepFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        handleAction();
        
        PostSleepViewModel viewModel = getViewModel();
        
        viewModel.init(getArgs().stoppedSessionData);
        
        mBinding.startValue.setText(viewModel.getStartText());
        
        mBinding.stopValue.setText(viewModel.getEndText());
        
        mBinding.duration.setText(viewModel.getDurationText());
        
        initMood();
        initTags();
        initAdditionalComments();
        initInterruptions();
        initRating();
        
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
        // The interruptions recycler gets added dynamically to post_sleep_interruptions_content
        // (FrameLayout), so no ViewBinding.
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
    
    private void initMood()
    {
        MoodUiData mood = getViewModel().getMood();
        
        if (mood != null) {
            mBinding.moodFrame.addView(createMoodView(mood));
        } else {
            TextView noMoodMessage = new TextView(
                    // root context used here so that the style works properly
                    mBinding.getRoot().getContext(),
                    null,
                    R.attr.trackerPostDialogNullDataMessageStyle);
            noMoodMessage.setText(R.string.postsleepdialog_nomood);
            mBinding.moodFrame.addView(noMoodMessage);
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
    
    private void initTags()
    {
        RecyclerView tagsRecycler = mBinding.tagsContent.getRecycler();
        
        FlexboxLayoutManager layoutManager =
                new FlexboxLayoutManager(mBinding.getRoot().getContext());
        tagsRecycler.setLayoutManager(layoutManager);
        
        SelectedTagAdapter adapter = new SelectedTagAdapter();
        tagsRecycler.setAdapter(adapter);
        
        LiveDataFuture.getValue(
                getViewModel().getTags(),
                getViewLifecycleOwner(),
                adapter::setSelectedTags);
    }
    
    private void initAdditionalComments()
    {
        if (getViewModel().hasAdditionalComments()) {
            mBinding.commentsValue.setText(getViewModel().getAdditionalComments());
        } else {
            // display 'No Comments' message instead
            mBinding.commentsValue.setVisibility(View.GONE);
            TextView noCommentsMessage = new TextView(
                    // root context used here for the style to work properly
                    mBinding.getRoot().getContext(),
                    null,
                    R.attr.trackerPostDialogNullDataMessageStyle);
            noCommentsMessage.setText(R.string.postsleepdialog_nocomments);
            
            mBinding.commentsScroll.removeAllViews();
            mBinding.commentsScroll.addView(noCommentsMessage);
        }
    }
    
    private void initInterruptions()
    {
        if (getViewModel().hasNoInterruptions()) {
            getLayoutInflater().inflate(R.layout.post_sleep_interruptions_nodata,
                                        mBinding.interruptionsContent);
        } else {
            initInterruptionsDataDisplay(mBinding.interruptionsContent);
        }
    }
    
    private void initInterruptionsDataDisplay(FrameLayout parent)
    {
        CommonInterruptionsBinding interruptions = CommonInterruptionsBinding.inflate(
                getLayoutInflater(), parent, true);
        
        interruptions.interruptionsCount.setText(getViewModel().getInterruptionsCountText());
        
        interruptions.interruptionsTotal.setText(getViewModel().getInterruptionsTotalTimeText());
        
        interruptions.interruptionsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        interruptions.interruptionsRecycler.setAdapter(new PostSleepInterruptionsAdapter(
                getViewModel().getInterruptionsListItems()));
    }
    
    private void initRating()
    {
        mBinding.starRating.setRating(getViewModel().getRating());
        mBinding.starRating.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
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
