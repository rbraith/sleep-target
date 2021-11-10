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
package com.rbraithwaite.sleeptarget.ui.sleep_goals;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.SleepGoalsFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DeleteDialog;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DurationPickerFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.streak_calendar.StreakCalendar;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import java.util.GregorianCalendar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepGoalsFragment
        extends BaseFragment<SleepGoalsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private SleepGoalsFragmentBinding mBinding;

//*********************************************************
// private constants
//*********************************************************

    private static final String WAKETIME_TIME_PICKER = "WakeTimeTimePicker";
    private static final String DIALOG_DELETE_WAKETIME = "DeleteWakeTime";
    private static final String DIALOG_DELETE_DURATION = "DeleteDuration";
    private static final String DIALOG_DURATION_HELP = "DurationHelp";
    private static final String DIALOG_WAKETIME_HELP = "WakeTimeHelp";
    private static final String PICKER_SLEEP_DURATION = "SleepDurationPicker";
    private static final String TAG = "SleepGoalsFragment";
    
    private static final String TAG_WAKETIME_TIME_SET = "WakeTimeSet";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class HelpDialog
            extends DialogFragment
    {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            @SuppressLint("InflateParams")
            View dialogView = getLayoutInflater().inflate(getArguments().getInt("layout"), null);
            builder.setTitle(getArguments().getInt("title"))
                    // TODO [21-08-29 6:30PM] -- add help dialog content.
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }
        
        public static HelpDialog createInstance(int titleId, int layoutId)
        {
            Bundle args = new Bundle();
            args.putInt("title", titleId);
            args.putInt("layout", layoutId);
            HelpDialog dialog = new HelpDialog();
            dialog.setArguments(args);
            return dialog;
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
        mBinding = SleepGoalsFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        initWakeTimeGoal();
        initSleepDurationGoal();
        initStreakCalendar();
    }
    
    @Override
    protected Properties<SleepGoalsFragmentViewModel> initProperties()
    {
        return new Properties<>(true, SleepGoalsFragmentViewModel.class);
    }

//*********************************************************
// private methods
//*********************************************************

    private void initStreakCalendar()
    {
        SleepGoalsFragmentViewModel viewModel = getViewModel();
        
        // use the frame context, so that the theme applied to the frame gets applied to the
        // calendar
        final StreakCalendar streakCalendar = new StreakCalendar(
                mBinding.targetHistory.sleepGoalsStreaksCalendarFrame.getContext(),
                viewModel::onCalendarMonthChanged);
        mBinding.targetHistory.sleepGoalsStreaksCalendarFrame.addView(streakCalendar.getView());
        
        // OPTIMIZE [21-03-14 10:44PM] -- right now I am using all succeeded goal dates in history -
        //  it would probably be better to only use those relevant to the currently displayed month
        //  of the calendar
        //  ---
        //  I would need new interfaces in the view model for finding succeeded goal dates within a
        //  range, and I would need a callback on the streak calendar, notifying when the user
        //  changes to a new month.
        viewModel.getSucceededTargetDates().observe(
                getViewLifecycleOwner(),
                succeededTargetDates -> streakCalendar.setSucceededGoalDates(
                        succeededTargetDates.wakeTimeDates,
                        succeededTargetDates.durationDates));
    }
    
    private void initWakeTimeGoal()
    {
        // REFACTOR [20-12-23 5:06PM] -- consider just moving the hasWakeTime() logic into
        //  getWakeTime()'s observer (branch on the String being null (no wake time)) inside
        //  initWakeTimeLayout().
        getViewModel().hasWakeTime().observe(
                getViewLifecycleOwner(),
                hasWakeTime -> {
                    // REFACTOR [21-06-16 7:23PM] hasWakeTime shouldn't be nullable.
                    if (hasWakeTime != null) {
                        if (hasWakeTime) {
                            // REFACTOR [21-06-16 7:25PM] call it setWakeTimeGoalIsDisplayed.
                            mBinding.waketime.getRoot().setVisibility(View.VISIBLE);
                            mBinding.newWaketimeBtn.setVisibility(View.GONE);
                        } else {
                            mBinding.newWaketimeBtn.setVisibility(View.VISIBLE);
                            mBinding.waketime.getRoot().setVisibility(View.GONE);
                        }
                    }
                });
        
        mBinding.newWaketimeBtn.setOnClickListener(v -> displayWakeTimePickerDialog(getViewModel().getDefaultWakeTime()));
        
        initWakeTimeLayout();
        
        View helpClickFrame = mBinding.waketimeHelpClickFrame;
        helpClickFrame.setOnClickListener(v -> displayWakeTimeHelpDialog());
        
        TimePickerFragment.ViewModel.getInstance(requireActivity()).onTimeSet().observe(
                getViewLifecycleOwner(),
                timeEvent -> {
                    if (timeEvent.isFreshForTag(TAG_WAKETIME_TIME_SET)) {
                        // REFACTOR [21-11-9 9:32PM] -- just pass the extra data into the view
                        //  model.
                        TimeOfDay timeOfDay = timeEvent.getExtra();
                        getViewModel().setWakeTime(timeOfDay.hourOfDay, timeOfDay.minute);
                    }
                });
        
        // handle waketime deletion from the dialog
        getActivityViewModel(DeleteDialog.Actions.class).onPositiveAction().observe(
                getViewLifecycleOwner(),
                event -> {
                    if (event.isFreshForTag(DIALOG_DELETE_WAKETIME)) {
                        getViewModel().clearWakeTime();
                    }
                });
    }
    
    // REFACTOR [21-01-29 2:46AM] -- duplicate logic with initWakeTimeGoal()
    private void initSleepDurationGoal()
    {
        mBinding.newDurationBtn.setOnClickListener(v -> displaySleepDurationGoalPickerDialog(
                getViewModel().getDefaultSleepDurationGoal()));
        
        // handle duration-set events from the duration picker dialog (could be adding or updating)
        DurationPickerFragment.ViewModel.getInstance(requireActivity())
                .onDurationSet()
                .observe(getViewLifecycleOwner(), durationEvent -> {
                    if (durationEvent.isFresh()) {
                        // REFACTOR [21-11-9 9:32PM] -- just pass the extra data into the view
                        //  model.
                        DurationPickerFragment.Data data = durationEvent.getExtra();
                        getViewModel().setSleepDurationGoal(data.hour, data.minute);
                    }
                });
        
        getViewModel().hasSleepDurationGoal().observe(
                getViewLifecycleOwner(),
                hasSleepDurationGoal -> {
                    // REFACTOR [21-06-16 7:24PM] hasSleepDurationGoal shouldn't be nullable.
                    if (hasSleepDurationGoal != null) {
                        // REFACTOR [21-06-16 7:25PM] call it setSleepDurationGoalIsDisplayed.
                        if (hasSleepDurationGoal) {
                            mBinding.duration.getRoot().setVisibility(View.VISIBLE);
                            mBinding.newDurationBtn.setVisibility(View.GONE);
                        } else {
                            mBinding.duration.getRoot().setVisibility(View.GONE);
                            mBinding.newDurationBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
        
        initSleepDurationGoalLayout();
        
        View helpClickFrame = mBinding.durationHelpClickFrame;
        helpClickFrame.setOnClickListener(v -> displaySleepDurationGoalHelpDialog());
        
        // handle duration deletion from the dialog
        getActivityViewModel(DeleteDialog.Actions.class).onPositiveAction().observe(
                getViewLifecycleOwner(),
                event -> {
                    if (event.isFreshForTag(DIALOG_DELETE_DURATION)) {
                        getViewModel().clearSleepDurationGoal();
                    }
                });
    }
    
    private void initSleepDurationGoalLayout()
    {
        getViewModel().getSleepDurationGoalText()
                .observe(getViewLifecycleOwner(), mBinding.duration.durationValue::setText);
        
        mBinding.duration.durationEditBtn.setOnClickListener(v -> LiveDataFuture.getValue(
                getViewModel().getSleepDurationGoal(),
                getViewLifecycleOwner(),
                this::displaySleepDurationGoalPickerDialog));
        
        mBinding.duration.durationDeleteBtn.setOnClickListener(v -> displaySleepDurationGoalDeleteDialog());
    }
    
    private void initWakeTimeLayout()
    {
        getViewModel().getWakeTimeText().observe(
                getViewLifecycleOwner(),
                mBinding.waketime.waketimeValue::setText);
        
        // REFACTOR [21-06-16 7:26PM] I might prefer to call this LiveDataUtils.getFuture().
        mBinding.waketime.waketimeEditBtn.setOnClickListener(v -> LiveDataFuture.getValue(
                getViewModel().getWakeTimeGoalDateMillis(),
                getViewLifecycleOwner(),
                // No null check needed since in theory this button should not
                // even be visible unless there is already a wake-time.
                this::displayWakeTimePickerDialog));
        
        mBinding.waketime.waketimeDeleteBtn.setOnClickListener(v -> displayWakeTimeDeleteDialog());
    }
    
    private void displayWakeTimeHelpDialog()
    {
        HelpDialog.createInstance(
                R.string.waketime_target_help_title,
                R.layout.sleep_goals_help_dialog_waketime)
                .show(getChildFragmentManager(), DIALOG_WAKETIME_HELP);
    }
    
    private void displaySleepDurationGoalHelpDialog()
    {
        HelpDialog.createInstance(
                R.string.duration_target_help_title,
                R.layout.sleep_goals_help_dialog_duration)
                .show(getChildFragmentManager(), DIALOG_DURATION_HELP);
    }
    
    private void displaySleepDurationGoalPickerDialog(SleepDurationGoalUIData initialValue)
    {
        DurationPickerFragment durationPickerDialog = DurationPickerFragment.createInstance(
                initialValue.hours,
                initialValue.remainingMinutes);
        durationPickerDialog.show(getChildFragmentManager(), PICKER_SLEEP_DURATION);
    }
    
    private void displaySleepDurationGoalDeleteDialog()
    {
        DeleteDialog.createInstance(
                DIALOG_DELETE_DURATION,
                R.string.sleep_goals_delete_duration_dialog_title,
                null)
                .show(getChildFragmentManager(), DIALOG_DELETE_DURATION);
    }
    
    private void displayWakeTimeDeleteDialog()
    {
        DeleteDialog.createInstance(
                DIALOG_DELETE_WAKETIME,
                R.string.sleep_goals_delete_waketime_dialog_title,
                null)
                .show(getChildFragmentManager(), DIALOG_DELETE_WAKETIME);
    }
    
    // REFACTOR [21-03-31 6:24PM] -- taking an absolute millis time is legacy behaviour - this
    //  should take hourOfDay & minute instead.
    private void displayWakeTimePickerDialog(long defaultValueDateMillis)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(defaultValueDateMillis);
        
        TimePickerFragment timePicker =
                TimePickerFragment.createInstance(TAG_WAKETIME_TIME_SET, TimeOfDay.of(cal));
        timePicker.show(getChildFragmentManager(), WAKETIME_TIME_PICKER);
    }
}
