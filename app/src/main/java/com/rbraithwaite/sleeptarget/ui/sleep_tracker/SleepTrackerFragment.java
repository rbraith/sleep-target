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
package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.rbraithwaite.sleeptarget.BuildConfig;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.TrackerFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepFragment;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
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

    private TrackerFragmentBinding mBinding;
    
    private MoodSelectorController mMoodSelectorController; // no gc
    private MoodSelectorViewModel mMoodSelectorViewModel;
    
    private TagSelectorViewModel mTagSelectorViewModel;
    
    private SleepTrackerAnimations mAnimations;

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
        mBinding = TrackerFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        mAnimations = new SleepTrackerAnimations(requireContext(), view);
        
        // must be called before any UI initialization
        handleAnyReturnFromPostSleep();
        
        initSessionTrackingDisplay();
        initInterruptions();
        initGoalsDisplay();
        
        // Details
        initAdditionalCommentsText();
        initMoodSelector();
        initTagSelector();
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        getViewModel().onPause();
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
        // TODO [21-10-19 3:07PM] This mood selector view model should be fragment-scoped.
        mMoodSelectorViewModel =
                CommonUtils.lazyInit(mMoodSelectorViewModel, MoodSelectorViewModel::new);
        return mMoodSelectorViewModel;
    }
    
    public TagSelectorViewModel getTagSelectorViewModel()
    {
        mTagSelectorViewModel = CommonUtils.lazyInit(
                mTagSelectorViewModel,
                () -> TagSelectorViewModel.getInstanceFrom(requireActivity()));
        return mTagSelectorViewModel;
    }

//*********************************************************
// private methods
//*********************************************************

    private void handleAnyReturnFromPostSleep()
    {
        getViewModel().handleAnyReturnFromPostSleep(getPostSleepViewModel());
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
    
    private void initTagSelector()
    {
        getViewModel().getInitialTagIds().observe(
                getViewLifecycleOwner(),
                initialTagIds -> {
                    TagSelectorViewModel tagSelectorViewModel = getTagSelectorViewModel();
                    
                    tagSelectorViewModel.setSelectedTagIds(initialTagIds);
                    
                    tagSelectorViewModel.getSelectedTags().observe(
                            getViewLifecycleOwner(),
                            getViewModel()::setSelectedTags);
    
                    mBinding.detailsContent.tags.init(
                            tagSelectorViewModel,
                            getViewLifecycleOwner(),
                            getChildFragmentManager());
                });
    }
    
    private void initMoodSelector()
    {
        getViewModel().getInitialMood().observe(
                getViewLifecycleOwner(),
                mood -> {
                    MoodSelectorViewModel moodSelectorViewModel =
                            MoodSelectorViewModel.getInstanceFrom(requireActivity());
                    moodSelectorViewModel.setMood(mood);
                    moodSelectorViewModel.getMood().observe(
                            getViewLifecycleOwner(),
                            getViewModel()::setMood);
                    
                    mMoodSelectorController = new MoodSelectorController(
                            mBinding.detailsContent.mood,
                            moodSelectorViewModel,
                            getViewLifecycleOwner(),
                            getChildFragmentManager());
                });
    }
    
    private void initAdditionalCommentsText()
    {
        getViewModel().getInitialAdditionalComments().observe(
                getViewLifecycleOwner(),
                additionalComments -> {
                    UiUtils.setEditTextValue(mBinding.detailsContent.additionalComments,
                                             additionalComments);
                    
                    mBinding.detailsContent.additionalComments.addTextChangedListener(new AfterTextChangedWatcher()
                    {
                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            getViewModel().setAdditionalComments(s.toString());
                        }
                    });
                });
    }
    
    private void initGoalsDisplay()
    {
        // wake-time goal
        mBinding.waketimeGoalContent.goalTitle.setText(R.string.tracker_goal_waketime_title);
        
        getViewModel().getWakeTimeGoalVisibility().observe(
                getViewLifecycleOwner(),
                mBinding.waketimeGoalCard::setVisibility);
        
        getViewModel().getWakeTimeGoalText().observe(
                getViewLifecycleOwner(),
                mBinding.waketimeGoalContent.value::setText);
        
        
        // sleep duration goal
        mBinding.durationGoalContent.goalTitle.setText(R.string.tracker_goal_duration_title);
        
        getViewModel().getSleepDurationGoalVisibility().observe(
                getViewLifecycleOwner(),
                mBinding.durationGoalCard::setVisibility);
        
        getViewModel().getSleepDurationGoalText().observe(
                getViewLifecycleOwner(),
                mBinding.durationGoalContent.value::setText);
        
        // no goals card
        getViewModel().getNoGoalsMessageVisibility().observe(
                getViewLifecycleOwner(),
                mBinding.noGoalsCard::setVisibility);
    }
    
    
    private void initSessionTrackingDisplay()
    {
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        
        Group startedTextGroup = mBinding.trackingContent.startedTextGroup;
        TextView currentSessionDuration = mBinding.trackingContent.sessionTime;
        
        // doing it like this so that the view visibility changes come after the animations
        viewModel.inSleepSession().observe(lifecycleOwner, inSleepSession -> {
            if (inSleepSession) {
                mAnimations.transitionIntoTrackingSession();
                startedTextGroup.setVisibility(View.VISIBLE);
                currentSessionDuration.setVisibility(View.VISIBLE);
            } else {
                mAnimations.transitionOutOfTrackingSession();
                startedTextGroup.setVisibility(View.GONE);
                currentSessionDuration.setVisibility(View.GONE);
            }
        });
        
        viewModel.getSleepTrackingButtonText().observe(
                getViewLifecycleOwner(),
                mBinding.trackingContent.trackerButton::setText);
        
        mBinding.trackingContent.trackerButton.setOnClickListener(v -> getViewModel().clickTrackingButton());
        
        AppColors appColors = AppColors.from(requireContext());
        viewModel.isSleepSessionInterrupted().observe(lifecycleOwner, isSleepSessionInterrupted -> {
            currentSessionDuration.setTextColor(isSleepSessionInterrupted ?
                                                        appColors.appColorOnPrimarySurface2 :
                                                        appColors.colorSecondary);
        });
        
        getViewModel().getCurrentSleepSessionDurationText().observe(
                lifecycleOwner,
                currentSessionDuration::setText);
        
        viewModel.getInterruptionsTotalText().observe(
                getViewLifecycleOwner(),
                mBinding.trackingContent.interruptionsTotal::setText);
        
        viewModel.getInterruptionsTotalVisibility().observe(
                getViewLifecycleOwner(),
                mBinding.trackingContent.interruptionsTotal::setVisibility);
        
        viewModel.getStartTimeText().observe(
                lifecycleOwner,
                mBinding.trackingContent.startTime::setText);
        
        viewModel.onNavToPostSleep().observe(
                lifecycleOwner,
                event -> {
                    if (event.isFresh()) {
                        navigateToPostSleepScreen(event.getExtra());
                    }
                });
    }
    
    private void initInterruptions()
    {
        SleepTrackerFragmentViewModel viewModel = getViewModel();
        
        // card
        viewModel.getInterruptionsVisibility().observe(
                getViewLifecycleOwner(),
                mBinding.interruptionsCard::setVisibility);
        
        // button, duration timer, reason
        TextView interruptDuration = mBinding.interruptionsContent.interruptDuration;
        EditText interruptionReasonText = mBinding.interruptionsContent.interruptReason;
        viewModel.isSleepSessionInterrupted().observe(
                getViewLifecycleOwner(),
                isSleepSessionInterrupted -> {
                    if (isSleepSessionInterrupted) {
                        mAnimations.transitionIntoInterruptionTimer();
                    } else {
                        // TODO [21-07-17 11:52PM] -- this is animating in a broken way right
                        //  now, so I'm not
                        //  using animations for this transition currently :(
//                        mAnimations.transitionOutOfInterruptionTimer();
                        interruptionReasonText.setVisibility(View.GONE);
                        interruptDuration.setVisibility(View.GONE);
                    }
                });
        
        viewModel.getInterruptButtonText().observe(
                getViewLifecycleOwner(),
                mBinding.interruptionsContent.interruptButton::setText);
        
        mBinding.interruptionsContent.interruptButton.setOnClickListener(v -> viewModel.clickInterruptionButton());
        
        viewModel.onInterruptionRecorded().observe(
                getViewLifecycleOwner(),
                event -> {
                    if (event.isFresh()) {
                        showInterruptionRecordedMessage();
                    }
                });
        
        viewModel.getOngoingInterruptionDuration().observe(
                getViewLifecycleOwner(),
                interruptDuration::setText);
        
        interruptionReasonText.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setInterruptionReason(s.toString());
            }
        });
        
        // initialize the interrupt reason from storage
        getViewModel().getInitialInterruptionReason().observe(
                getViewLifecycleOwner(), reason -> {
                    UiUtils.setEditTextValue(interruptionReasonText, reason);
                });
    }
    
    private void showInterruptionRecordedMessage()
    {
        LiveDataFuture.getValue(
                getViewModel().getLastInterruptionDuration(),
                getViewLifecycleOwner(),
                durationText -> Snackbar.make(
                        mBinding.interruptionsContent.interruptButton,
                        // REFACTOR [21-07-18 12:01AM] -- hardcoded text.
                        String.format(Locale.CANADA, "%s interruption recorded.", durationText),
                        Snackbar.LENGTH_SHORT)
                        .show());
    }
    
    private void navigateToPostSleepScreen(StoppedSessionData stoppedSessionData)
    {
        getNavController().navigate(SleepTrackerFragmentDirections.actionSleeptrackerToPostsleep(
                new PostSleepFragment.Args(stoppedSessionData)));
    }
    
    // REFACTOR [20-11-15 1:55AM] -- should extract this as a general utility.
    private boolean handleNavigationMenuItem(MenuItem item)
    {
        return NavigationUI.onNavDestinationSelected(item, getNavController())
               || super.onOptionsItemSelected(item);
    }
}
