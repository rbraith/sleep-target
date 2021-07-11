package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavDirections;
import androidx.navigation.ui.NavigationUI;

import com.rbraithwaite.sleepapp.BuildConfig;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepTrackerFragment
        extends BaseFragment<SleepTrackerFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private EditText mAdditionalComments;
    
    private MoodSelectorController mMoodSelectorController;
    private MoodSelectorViewModel mMoodSelectorViewModel;
    
    private TagSelectorController mTagSelectorController;
    private TagSelectorViewModel mTagSelectorViewModel;
    
    private SleepTrackerAnimations mAnimations;

//*********************************************************
// public constants
//*********************************************************

    public static final String POST_SLEEP_DIALOG = "PostSleepDialog";
    
    public static final String POST_SLEEP_DISCARD_DIALOG = "PostSleepDiscardDialog";

//*********************************************************
// constructors
//*********************************************************

    public SleepTrackerFragment()
    {
        setHasOptionsMenu(true);
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
        return inflater.inflate(R.layout.tracker_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        mAnimations = new SleepTrackerAnimations(requireContext(), view);
        
        initSessionTrackingDisplay(view);
        initInterruptions(view);
        
        initGoalsDisplay(view);
        
        // Details
        initAdditionalCommentsText(view);
        initMoodSelector(view);
        initTagSelector(view);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        getViewModel().persistLocalValues();
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.sleeptracker_menu, menu);
        
        if (BuildConfig.DEBUG) {
            addDevToolsOptionTo(menu);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
        default:
            return handleNavigationMenuItem(item);
        }
    }
    
    @Override
    protected Properties<SleepTrackerFragmentViewModel> initProperties()
    {
        return new Properties<>(true, SleepTrackerFragmentViewModel.class);
    }

//*********************************************************
// api
//*********************************************************

    public MoodSelectorViewModel getMoodSelectorViewModel()
    {
        return mMoodSelectorViewModel;
    }
    
    public TagSelectorViewModel getTagSelectorViewModel()
    {
        return mTagSelectorViewModel;
    }

//*********************************************************
// private methods
//*********************************************************

    private void clearDetailsValues()
    {
        mAdditionalComments.getText().clear();
        mTagSelectorViewModel.clearSelectedTags();
        mMoodSelectorViewModel.clearSelectedMood();
    }
    
    private void addDevToolsOptionTo(Menu menu)
    {
        MenuItem devToolsOption = menu.add("Dev Tools");
        devToolsOption.setOnMenuItemClickListener(item -> {
            NavDirections toDevTools =
                    SleepTrackerFragmentDirections.actionNavSleeptrackerToDebugNavgraph();
            getNavController().navigate(toDevTools);
            return true;
        });
    }
    
    private void initTagSelector(final View fragmentRoot)
    {
        mTagSelectorViewModel = new TagSelectorViewModel(requireContext());
        
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        
        mTagSelectorViewModel.getSelectedTags().observe(
                getViewLifecycleOwner(),
                viewModel::setLocalSelectedTags);
        
        mTagSelectorController = new TagSelectorController(
                fragmentRoot.findViewById(R.id.more_context_tags),
                mTagSelectorViewModel,
                getViewLifecycleOwner(),
                requireContext(),
                getChildFragmentManager());
        
        // initialize the tag selector's selected tags
        viewModel.getPersistedSelectedTagIds().observe(
                getViewLifecycleOwner(), mTagSelectorViewModel::setSelectedTagIds);
    }
    
    private void initMoodSelector(View fragmentRoot)
    {
        mMoodSelectorViewModel = new MoodSelectorViewModel();
        mMoodSelectorController = new MoodSelectorController(
                fragmentRoot.findViewById(R.id.more_context_mood),
                mMoodSelectorViewModel,
                getViewLifecycleOwner(),
                getChildFragmentManager());
        // REFACTOR [21-04-12 2:52AM] -- Like with the tag selector, these callbacks should come
        //  from the view model, probably as LiveData - anything having to do with the view state
        //  should be managed by the view model.
        mMoodSelectorController.setCallbacks(new MoodSelectorController.Callbacks()
        {
            @Override
            public void onMoodChanged(MoodUiData newMood)
            {
                getViewModel().setLocalMood(newMood);
            }
            
            @Override
            public void onMoodDeleted()
            {
                getViewModel().clearLocalMood();
            }
        });
        
        // initialize the mood selector's selected mood
        getViewModel().getPersistedMood()
                .observe(getViewLifecycleOwner(), mMoodSelectorViewModel::setMood);
    }
    
    private void initAdditionalCommentsText(View fragmentRoot)
    {
        mAdditionalComments = fragmentRoot.findViewById(R.id.additional_comments);
        
        mAdditionalComments.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setLocalAdditionalComments(s.toString());
            }
        });
        
        // initialize the additional comments value
        getViewModel().getPersistedAdditionalComments().observe(
                getViewLifecycleOwner(),
                additionalComments -> {
                    mAdditionalComments.getText().clear();
                    if (additionalComments != null) {
                        mAdditionalComments.getText().append(additionalComments);
                    }
                });
    }
    
    private void initGoalsDisplay(View fragmentRoot)
    {
        // wake-time goal
        View wakeTimeGoalCard = fragmentRoot.findViewById(R.id.tracker_waketime_goal_card);
        final TextView wakeTimeGoalTitle = wakeTimeGoalCard.findViewById(R.id.tracker_goal_title);
        final TextView wakeTimeGoalValue = wakeTimeGoalCard.findViewById(R.id.tracker_goal_value);
        
        wakeTimeGoalTitle.setText(R.string.tracker_goal_waketime_title);
        
        getViewModel().getWakeTimeGoalText().observe(
                getViewLifecycleOwner(),
                wakeTimeGoalText -> {
                    if (wakeTimeGoalText == null) {
                        wakeTimeGoalCard.setVisibility(View.GONE);
                    } else {
                        wakeTimeGoalCard.setVisibility(View.VISIBLE);
                        wakeTimeGoalValue.setText(wakeTimeGoalText);
                    }
                });
        
        // sleep duration goal
        View sleepDurationGoalCard = fragmentRoot.findViewById(R.id.tracker_duration_goal_card);
        final TextView sleepDurationGoalTitle =
                sleepDurationGoalCard.findViewById(R.id.tracker_goal_title);
        final TextView sleepDurationGoalValue =
                sleepDurationGoalCard.findViewById(R.id.tracker_goal_value);
        
        sleepDurationGoalTitle.setText(R.string.tracker_goal_duration_title);
        
        getViewModel().getSleepDurationGoalText().observe(
                getViewLifecycleOwner(),
                sleepDurationGoalText -> {
                    if (sleepDurationGoalText == null) {
                        sleepDurationGoalCard.setVisibility(View.GONE);
                    } else {
                        sleepDurationGoalCard.setVisibility(View.VISIBLE);
                        sleepDurationGoalValue.setText(sleepDurationGoalText);
                    }
                });
        
        // no goals card
        CardView noGoalsCard = fragmentRoot.findViewById(R.id.tracker_no_goals_card);
        getViewModel().hasAnyGoal().observe(
                getViewLifecycleOwner(),
                hasAnyGoal -> noGoalsCard.setVisibility(hasAnyGoal ? View.GONE : View.VISIBLE));
    }
    
    private void initSessionTrackingDisplay(View fragmentRoot)
    {
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        
        Group startTimeGroup = fragmentRoot.findViewById(R.id.started_text_group);
        TextView currentSessionTimeText =
                fragmentRoot.findViewById(R.id.sleep_tracker_session_time);
        Button sleepTrackingButton = fragmentRoot.findViewById(R.id.sleep_tracker_button);
        
        viewModel.inSleepSession().observe(lifecycleOwner, inSleepSession -> {
            if (inSleepSession) {
                mAnimations.transitionIntoTrackingSession();
                startTimeGroup.setVisibility(View.VISIBLE);
                currentSessionTimeText.setVisibility(View.VISIBLE);
                sleepTrackingButton.setText(R.string.sleep_tracker_button_stop);
            } else {
                mAnimations.transitionOutOfTrackingSession();
                startTimeGroup.setVisibility(View.GONE);
                currentSessionTimeText.setVisibility(View.GONE);
                sleepTrackingButton.setText(R.string.sleep_tracker_button_start);
            }
        });
        
        TextView startTimeText = fragmentRoot.findViewById(R.id.sleep_tracker_start_time);
        viewModel.getSessionStartTime().observe(lifecycleOwner, startTimeText::setText);
        
        getViewModel().getCurrentSleepSessionDuration().observe(
                lifecycleOwner,
                currentSessionTimeText::setText);
        
        // REFACTOR [21-07-4 1:08AM] -- consider extracting this to a new method -
        //  maybe createSleepTrackingButtonClickListener.
        sleepTrackingButton.setOnClickListener(v -> {
            if (viewModel.inSleepSession().getValue()) {
                viewModel.stopSleepSession();
                clearDetailsValues();
                displayPostSleepDialog(viewModel.getStoppedSessionData());
            } else {
                viewModel.startSleepSession();
            }
        });
    }
    
    private void initInterruptions(View fragmentRoot)
    {
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        
        // card
        CardView interruptionsCard = fragmentRoot.findViewById(R.id.tracker_interruptions_card);
        viewModel.inSleepSession().observe(
                getViewLifecycleOwner(),
                inSleepSession -> {
                    interruptionsCard.setVisibility(inSleepSession ? View.VISIBLE : View.GONE);
                });
        
        // button
        Button interruptButton = fragmentRoot.findViewById(R.id.tracker_interrupt_button);
        viewModel.isSleepSessionInterrupted().observe(
                getViewLifecycleOwner(),
                isSleepSessionInterrupted -> {
                    interruptButton.setOnClickListener(isSleepSessionInterrupted ?
                                                               v -> viewModel.resumeSleepSession() :
                                                               v -> viewModel.interruptSleepSession());
                });
        
        // reason text field
        EditText interruptionReasonText = fragmentRoot.findViewById(R.id.tracker_interrupt_reason);
        
        interruptionReasonText.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setLocalInterruptionReason(s.toString());
            }
        });
        
        // initialize the interrupt reason from storage
        getViewModel().getPersistedInterruptionReason().observe(
                getViewLifecycleOwner(), reason -> {
                    interruptionReasonText.getText().clear();
                    if (reason != null) {
                        interruptionReasonText.getText().append(reason);
                    }
                });
    }
    
    private void displayPostSleepDialog(StoppedSessionData stoppedSession)
    {
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        
        PostSleepDialogViewModel dialogViewModel =
                new PostSleepDialogViewModel(stoppedSession, requireContext());
        // OPTIMIZE [21-05-8 6:00PM] -- The post sleep data in displayPostSleepDialog is coming
        //  from the sleep tracker view model originally, so it unnecessarily updates to its
        //  own value here.
        dialogViewModel.getPostSleepData().observe(
                getViewLifecycleOwner(),
                viewModel::setPostSleepData);
        
        PostSleepDialog dialog = PostSleepDialog.createInstance(
                dialogViewModel,
                viewModel::keepStoppedSession,
                this::displaySessionDiscardDialog);
        dialog.show(getChildFragmentManager(), POST_SLEEP_DIALOG);
    }
    
    private void displaySessionDiscardDialog()
    {
        AlertDialogFragment discardDialog =
                AlertDialogFragment.createInstance(() -> {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(requireContext());
                    builder.setTitle(R.string.postsleep_discard_title)
                            .setMessage(R.string.postsleep_discard_warning)
                            // If the user cancels the discard, show the post sleep
                            // dialog again
                            .setNegativeButton(R.string.cancel,
                                               ((dialog1, which) -> displayPostSleepDialog(
                                                       getViewModel().getStoppedSessionData())))
                            .setPositiveButton(R.string.discard,
                                               (dialog1, which) -> getViewModel().discardSleepSession());
                    
                    AlertDialog alertDialog = builder.create();
                    // This dialog can't be cancelled by clicking outside since this is
                    // a kind of "point of no return" - the user must now either record
                    // the session to the archive or discard it.
                    alertDialog.setCanceledOnTouchOutside(false);
                    return alertDialog;
                });
        discardDialog.show(getChildFragmentManager(), POST_SLEEP_DISCARD_DIALOG);
    }
    
    // REFACTOR [20-11-15 1:55AM] -- should extract this as a general utility.
    private boolean handleNavigationMenuItem(MenuItem item)
    {
        return NavigationUI.onNavDestinationSelected(item, getNavController())
               || super.onOptionsItemSelected(item);
    }
}
