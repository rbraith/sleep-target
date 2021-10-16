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

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DialogUtils;
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
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.sleep_goals_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        initWakeTimeGoal(view);
        initSleepDurationGoal(view);
        initStreakCalendar(view);
    }
    
    @Override
    protected Properties<SleepGoalsFragmentViewModel> initProperties()
    {
        return new Properties<>(true, SleepGoalsFragmentViewModel.class);
    }

//*********************************************************
// private methods
//*********************************************************

    private void initStreakCalendar(View fragmentRoot)
    {
        SleepGoalsFragmentViewModel viewModel = getViewModel();
        
        FrameLayout streakCalendarFrame =
                fragmentRoot.findViewById(R.id.sleep_goals_streaks_calendar_frame);
        // use the frame context, so that the theme applied to the frame gets applied to the
        // calendar
        final StreakCalendar streakCalendar = new StreakCalendar(
                streakCalendarFrame.getContext(),
                viewModel::onCalendarMonthChanged);
        streakCalendarFrame.addView(streakCalendar.getView());
        
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
    
    private void initWakeTimeGoal(View fragmentRoot)
    {
        CardView wakeTimeCard = fragmentRoot.findViewById(R.id.sleep_goals_waketime_card);
        final View wakeTimeLayout = wakeTimeCard.findViewById(R.id.sleep_goals_waketime);
        final Button buttonAddNewWakeTime =
                wakeTimeCard.findViewById(R.id.sleep_goals_new_waketime_btn);
        
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
                            wakeTimeLayout.setVisibility(View.VISIBLE);
                            buttonAddNewWakeTime.setVisibility(View.GONE);
                        } else {
                            buttonAddNewWakeTime.setVisibility(View.VISIBLE);
                            wakeTimeLayout.setVisibility(View.GONE);
                        }
                    }
                });
        
        buttonAddNewWakeTime.setOnClickListener(v -> displayWakeTimePickerDialog(getViewModel().getDefaultWakeTime()));
        
        initWakeTimeLayout(wakeTimeLayout);
        
        View helpClickFrame = wakeTimeCard.findViewById(R.id.sleep_goals_waketime_help_click_frame);
        helpClickFrame.setOnClickListener(v -> displayWakeTimeHelpDialog());
        
        TimePickerFragment.ViewModel.getInstance(requireActivity()).onTimeSet().observe(
                getViewLifecycleOwner(),
                timeEvent -> {
                    if (timeEvent.isFreshForTag(TAG_WAKETIME_TIME_SET)) {
                        TimeOfDay timeOfDay = timeEvent.getExtra();
                        getViewModel().setWakeTime(timeOfDay.hourOfDay, timeOfDay.minute);
                    }
                });
    }
    
    // REFACTOR [21-01-29 2:46AM] -- duplicate logic with initWakeTimeGoal()
    private void initSleepDurationGoal(View fragmentRoot)
    {
        CardView sleepDurationCard = fragmentRoot.findViewById(R.id.sleep_goals_duration_card);
        final View sleepDurationGoalLayout =
                sleepDurationCard.findViewById(R.id.sleep_goals_duration);
        final Button buttonAddNewSleepDuration =
                sleepDurationCard.findViewById(R.id.sleep_goals_new_duration_btn);
        buttonAddNewSleepDuration.setOnClickListener(v -> displaySleepDurationGoalPickerDialog(
                getViewModel().getDefaultSleepDurationGoal()));
        
        getViewModel().hasSleepDurationGoal().observe(
                getViewLifecycleOwner(),
                hasSleepDurationGoal -> {
                    // REFACTOR [21-06-16 7:24PM] hasSleepDurationGoal shouldn't be nullable.
                    if (hasSleepDurationGoal != null) {
                        // REFACTOR [21-06-16 7:25PM] call it setSleepDurationGoalIsDisplayed.
                        if (hasSleepDurationGoal) {
                            sleepDurationGoalLayout.setVisibility(View.VISIBLE);
                            buttonAddNewSleepDuration.setVisibility(View.GONE);
                        } else {
                            sleepDurationGoalLayout.setVisibility(View.GONE);
                            buttonAddNewSleepDuration.setVisibility(View.VISIBLE);
                        }
                    }
                });
        
        initSleepDurationGoalLayout(sleepDurationGoalLayout);
        
        View helpClickFrame =
                sleepDurationCard.findViewById(R.id.sleep_goals_duration_help_click_frame);
        helpClickFrame.setOnClickListener(v -> displaySleepDurationGoalHelpDialog());
    }
    
    private void initSleepDurationGoalLayout(View sleepDurationGoalLayout)
    {
        final TextView valueText =
                sleepDurationGoalLayout.findViewById(R.id.duration_value);
        getViewModel().getSleepDurationGoalText()
                .observe(getViewLifecycleOwner(), valueText::setText);
        
        Button editButton = sleepDurationGoalLayout.findViewById(R.id.duration_edit_btn);
        editButton.setOnClickListener(v -> LiveDataFuture.getValue(
                getViewModel().getSleepDurationGoal(),
                getViewLifecycleOwner(),
                this::displaySleepDurationGoalPickerDialog));
        
        Button deleteButton = sleepDurationGoalLayout.findViewById(R.id.duration_delete_btn);
        deleteButton.setOnClickListener(v -> displaySleepDurationGoalDeleteDialog());
    }
    
    
    private void initWakeTimeLayout(View wakeTimeLayout)
    {
        final TextView wakeTimeValue = wakeTimeLayout.findViewById(R.id.waketime_value);
        getViewModel().getWakeTimeText().observe(
                getViewLifecycleOwner(),
                wakeTimeValue::setText);
        Button wakeTimeEditButton = wakeTimeLayout.findViewById(R.id.waketime_edit_btn);
        // REFACTOR [21-06-16 7:26PM] I might prefer to call this LiveDataUtils.getFuture().
        wakeTimeEditButton.setOnClickListener(v -> LiveDataFuture.getValue(
                getViewModel().getWakeTimeGoalDateMillis(),
                getViewLifecycleOwner(),
                // No null check needed since in theory this button should not
                // even be visible unless there is already a wake-time.
                this::displayWakeTimePickerDialog));
        Button wakeTimeDeleteButton = wakeTimeLayout.findViewById(R.id.waketime_delete_btn);
        wakeTimeDeleteButton.setOnClickListener(v -> displayWakeTimeDeleteDialog());
    }
    
    private void displayWakeTimeHelpDialog()
    {
        DialogFragment dialog = AlertDialogFragment.createInstance((context, inflater) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("How do I hit a wake-time target?")
                    .setView(inflater.inflate(R.layout.sleep_goals_help_dialog_waketime,
                                              null))
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        
        dialog.show(getChildFragmentManager(), DIALOG_WAKETIME_HELP);
    }
    
    private void displaySleepDurationGoalHelpDialog()
    {
        DialogFragment dialog = AlertDialogFragment.createInstance((context, inflater) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("How do I hit a sleep duration target?")
                    // TODO [21-08-29 6:30PM] -- add help dialog content.
                    .setView(inflater.inflate(R.layout.sleep_goals_help_dialog_duration,
                                              null))
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        
        dialog.show(getChildFragmentManager(), DIALOG_DURATION_HELP);
    }
    
    private void displaySleepDurationGoalPickerDialog(SleepDurationGoalUIData initialValue)
    {
        DurationPickerFragment durationPickerDialog = DurationPickerFragment.createInstance(
                initialValue.hours,
                initialValue.remainingMinutes,
                // BUG [21-09-9 5:10PM] -- This getViewModel() call in this lambda (and other
                //  similar calls in other lambdas) is likely causing a memory leak when the
                //  device is rotated (the lambda, stored in the dialog fragment, retains a ref
                //  to the old SleepGoalsFragment)
                //  see: https://www.logicbig.com/tutorials/core-java-tutorial/java-language
                //  /implicit-outer-class-reference.html.
                (dialog, which, hour, minute) -> getViewModel().setSleepDurationGoal(hour, minute));
        durationPickerDialog.show(getChildFragmentManager(), PICKER_SLEEP_DURATION);
    }
    
    private void displaySleepDurationGoalDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        R.string.sleep_goals_delete_duration_dialog_title,
                        (dialog, which) -> getViewModel().clearSleepDurationGoal())
                .show(getChildFragmentManager(), DIALOG_DELETE_DURATION);
        ;
    }
    
    private void displayWakeTimeDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        R.string.sleep_goals_delete_waketime_dialog_title,
                        (dialog, which) -> getViewModel().clearWakeTime())
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
