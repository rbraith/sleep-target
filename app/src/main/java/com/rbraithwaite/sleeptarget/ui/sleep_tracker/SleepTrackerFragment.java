/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.rbraithwaite.sleeptarget.BuildConfig;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorController;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.utils.AppColors;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepTrackerFragment
        extends BaseFragment<SleepTrackerFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private EditText mInterruptionReasonText;
    private EditText mAdditionalComments;
    
    private MoodSelectorController mMoodSelectorController;
    private MoodSelectorViewModel mMoodSelectorViewModel;
    
    private TagSelectorController mTagSelectorController;
    private TagSelectorViewModel mTagSelectorViewModel;
    
    private SleepTrackerAnimations mAnimations;
    
    private boolean mInSleepSession = false;

//*********************************************************
// public constants
//*********************************************************

    public static final String POST_SLEEP_DIALOG = "PostSleepDialog";

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
        
        // these are the views that need non-local refs
        mAdditionalComments = view.findViewById(R.id.additional_comments);
        mInterruptionReasonText = view.findViewById(R.id.tracker_interrupt_reason);
        
        // must be called before any UI initialization
        handleAnyReturnFromPostSleep();
        
        initSessionTrackingDisplay(view);
        initInterruptions(view);
        initGoalsDisplay(view);
        
        // Details
        initAdditionalCommentsText();
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
        mMoodSelectorViewModel =
                CommonUtils.lazyInit(mMoodSelectorViewModel, MoodSelectorViewModel::new);
        return mMoodSelectorViewModel;
    }
    
    public TagSelectorViewModel getTagSelectorViewModel()
    {
        mTagSelectorViewModel = CommonUtils.lazyInit(
                mTagSelectorViewModel, () -> new TagSelectorViewModel(requireContext()));
        return mTagSelectorViewModel;
    }

//*********************************************************
// private methods
//*********************************************************

    private void handleAnyReturnFromPostSleep()
    {
        PostSleepViewModel postSleepViewModel = getPostSleepViewModel();
        int action = postSleepViewModel.consumeAction();
        
        switch (action) {
        case PostSleepViewModel.NO_ACTION:
            return; // do nothing
        case PostSleepViewModel.KEEP:
            getViewModel().keepSleepSession(postSleepViewModel.consumeData());
            return;
        case PostSleepViewModel.DISCARD:
            getViewModel().clearSleepSession();
            postSleepViewModel.discardData();
        }
    }
    
    // HACK [21-08-17 3:12AM] -- This is abusing the fact that BaseFragment is currently using
    //  Activity-scoped view models, so this instance is ensured to be the same as the one used in
    //  PostSleepFragment. Preferably the results logic should be in a separate shared view model.
    private PostSleepViewModel getPostSleepViewModel()
    {
        return new ViewModelProvider(requireActivity()).get(PostSleepViewModel.class);
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
        getViewModel().getInitialTagIds().observe(
                getViewLifecycleOwner(),
                initialTagIds -> {
                    getTagSelectorViewModel().setSelectedTagIds(initialTagIds);
                    getTagSelectorViewModel().getSelectedTags().observe(
                            getViewLifecycleOwner(),
                            getViewModel()::setLocalSelectedTags);
                    
                    mTagSelectorController = new TagSelectorController(
                            fragmentRoot.findViewById(R.id.more_context_tags),
                            getTagSelectorViewModel(),
                            getViewLifecycleOwner(),
                            getChildFragmentManager());
                });
    }
    
    private void initMoodSelector(View fragmentRoot)
    {
        getViewModel().getInitialMood().observe(
                getViewLifecycleOwner(),
                mood -> {
                    getMoodSelectorViewModel().setMood(mood);
                    
                    mMoodSelectorController = new MoodSelectorController(
                            fragmentRoot.findViewById(R.id.more_context_mood),
                            getMoodSelectorViewModel(),
                            getViewLifecycleOwner(),
                            getChildFragmentManager());
                    // REFACTOR [21-04-12 2:52AM] -- Like with the tag selector, these callbacks
                    //  should come
                    //  from the view model, probably as LiveData - anything having to do with
                    //  the view state
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
                });
    }
    
    private void initAdditionalCommentsText()
    {
        getViewModel().getInitialAdditionalComments().observe(
                getViewLifecycleOwner(),
                additionalComments -> {
                    UiUtils.setEditTextValue(mAdditionalComments, additionalComments);
                    
                    mAdditionalComments.addTextChangedListener(new AfterTextChangedWatcher()
                    {
                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            getViewModel().setLocalAdditionalComments(s.toString());
                        }
                    });
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
            mInSleepSession = inSleepSession;
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
        
        AppColors appColors = AppColors.from(requireContext());
        viewModel.isSleepSessionInterrupted().observe(lifecycleOwner, isSleepSessionInterrupted -> {
            currentSessionTimeText.setTextColor(isSleepSessionInterrupted ?
                                                        appColors.appColorOnPrimarySurface2 :
                                                        appColors.colorSecondary);
        });
        
        TextView interruptionsTotalText =
                fragmentRoot.findViewById(R.id.sleep_tracker_interruptions_total);
        viewModel.getInterruptionsTotal().observe(lifecycleOwner, interruptionsTotal -> {
            if (interruptionsTotal != null && mInSleepSession) {
                interruptionsTotalText.setVisibility(View.VISIBLE);
                interruptionsTotalText.setText(interruptionsTotal);
            } else {
                interruptionsTotalText.setVisibility(View.GONE);
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
                navigateToPostSleepScreen();
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
        
        // button, duration timer, reason
        Button interruptButton = fragmentRoot.findViewById(R.id.tracker_interrupt_button);
        TextView interruptDuration = fragmentRoot.findViewById(R.id.tracker_interrupt_duration);
        viewModel.isSleepSessionInterrupted().observe(
                getViewLifecycleOwner(),
                isSleepSessionInterrupted -> {
                    if (isSleepSessionInterrupted) {
                        mAnimations.transitionIntoInterruptionTimer();
                        interruptButton.setText(R.string.tracker_interrupt_btn_resume);
                        interruptButton.setOnClickListener(v -> {
                            viewModel.resumeSleepSession();
                            showInterruptionRecordedMessage(v);
                        });
                    } else {
                        // TODO [21-07-17 11:52PM] -- this is animating in a broken way right
                        //  now, so I'm not
                        //  using animations for this transition currently :(
//                        mAnimations.transitionOutOfInterruptionTimer();
                        mInterruptionReasonText.setVisibility(View.GONE);
                        interruptDuration.setVisibility(View.GONE);
                        
                        interruptButton.setText(R.string.tracker_interrupt_btn_interrupt);
                        interruptButton.setOnClickListener(v -> viewModel.interruptSleepSession());
                    }
                });
        
        viewModel.getOngoingInterruptionDuration().observe(
                getViewLifecycleOwner(),
                interruptDuration::setText);
        
        mInterruptionReasonText.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setLocalInterruptionReason(s.toString());
            }
        });
        
        // initialize the interrupt reason from storage
        getViewModel().getInitialInterruptionReason().observe(
                getViewLifecycleOwner(), reason -> {
                    mInterruptionReasonText.getText().clear();
                    if (reason != null) {
                        mInterruptionReasonText.getText().append(reason);
                        getViewModel().setLocalInterruptionReason(reason);
                    }
                });
    }
    
    private void showInterruptionRecordedMessage(View v)
    {
        LiveDataFuture.getValue(
                getViewModel().getLastInterruptionDuration(),
                getViewLifecycleOwner(),
                durationText -> Snackbar.make(
                        v,
                        // REFACTOR [21-07-18 12:01AM] -- hardcoded text.
                        String.format(Locale.CANADA, "%s interruption recorded.", durationText),
                        Snackbar.LENGTH_SHORT)
                        .show());
    }
    
    private void navigateToPostSleepScreen()
    {
        LiveDataFuture.getValue(
                getViewModel().getSleepSessionSnapshot(),
                getViewLifecycleOwner(),
                snapshot -> getNavController().navigate(SleepTrackerFragmentDirections.actionSleeptrackerToPostsleep(
                        new PostSleepFragment.Args(snapshot))));
    }
    
    // REFACTOR [20-11-15 1:55AM] -- should extract this as a general utility.
    private boolean handleNavigationMenuItem(MenuItem item)
    {
        return NavigationUI.onNavDestinationSelected(item, getNavController())
               || super.onOptionsItemSelected(item);
    }
}
