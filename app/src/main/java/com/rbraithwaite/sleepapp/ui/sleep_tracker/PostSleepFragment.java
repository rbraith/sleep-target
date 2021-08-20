package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.ActionFragment;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.SelectedTagAdapter;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleepapp.ui.utils.UiUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

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
    
    public static class Args implements Serializable
    {
        public static final long serialVersionUID = 20210112L;
        
        public StoppedSessionData stoppedSessionData;
        
        public Args(StoppedSessionData stoppedSessionData)
        {
            this.stoppedSessionData = stoppedSessionData;
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
        return getView().findViewById(R.id.common_interruptions_recycler);
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
            
            ScrollView commentsScroll = fragmentRoot.findViewById(R.id.postsleep_comments_scroll);
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
        
        TextView countText = interruptionsLayout.findViewById(R.id.common_interruptions_count);
        countText.setText(getViewModel().getInterruptionsCountText());
        
        TextView totalTimeText =
                interruptionsLayout.findViewById(R.id.common_interruptions_total);
        totalTimeText.setText(getViewModel().getInterruptionsTotalTimeText());
        
        RecyclerView interruptionsRecycler =
                interruptionsLayout.findViewById(R.id.common_interruptions_recycler);
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
        AlertDialogFragment discardDialog =
                AlertDialogFragment.createInstance(() -> {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(requireContext());
                    builder.setTitle(R.string.postsleep_discard_title)
                            .setMessage(R.string.postsleep_discard_warning)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.discard, (dialog1, which) -> onDiscard());
                    return builder.create();
                });
        discardDialog.show(getChildFragmentManager(), POST_SLEEP_DISCARD_DIALOG);
    }
    
    private void onDiscard()
    {
        getViewModel().setAction(PostSleepViewModel.DISCARD);
        navigateUp();
    }
}
