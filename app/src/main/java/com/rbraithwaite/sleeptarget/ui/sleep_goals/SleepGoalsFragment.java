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
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DialogUtils;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DurationPickerFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.streak_calendar.StreakCalendar;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
        FrameLayout streakCalendarFrame =
                fragmentRoot.findViewById(R.id.sleep_goals_streaks_calendar_frame);
        // use the frame context, so that the theme applied to the frame gets applied to the
        // calendar
        final StreakCalendar streakCalendar = new StreakCalendar(streakCalendarFrame.getContext());
        streakCalendarFrame.addView(streakCalendar.getView());
        
        SleepGoalsFragmentViewModel viewModel = getViewModel();
        // REFACTOR [21-06-16 7:21PM] the viewmodel should directly pass the data needed,
        //  In this case a List of maybe some SucceededGoalDate data classes, w/ the date & which
        //  goals succeeded.
        LiveData<List<List<Date>>> succeededGoalDates = LiveDataUtils.merge(
                viewModel.getSucceededWakeTimeGoalDates(),
                viewModel.getSucceededSleepDurationGoalDates(),
                (wakeTimeGoalDates, sleepDurationGoalDates) -> Arrays.asList(wakeTimeGoalDates,
                                                                             sleepDurationGoalDates));
        
        // OPTIMIZE [21-03-14 10:44PM] -- right now I am using all succeeded goal dates in history -
        //  it would probably be better to only use those relevant to the currently displayed month
        //  of the calendar
        //  ---
        //  I would need new interfaces in the view model for finding succeeded goal dates within a
        //  range, and I would need a callback on the streak calendar, notifying when the user
        //  changes to a new month.
        succeededGoalDates.observe(
                getViewLifecycleOwner(),
                bothGoalDates -> {
                    List<Date> wakeTimeGoalDates = bothGoalDates.get(0);
                    List<Date> sleepDurationGoalDates = bothGoalDates.get(1);
                    
                    streakCalendar.setSucceededGoalDates(wakeTimeGoalDates,
                                                         sleepDurationGoalDates);
                });
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
    }
    
    // REFACTOR [21-01-29 2:46AM] -- duplicate logic with initWakeTimeGoal()
    private void initSleepDurationGoal(View fragmentRoot)
    {
        CardView sleepDurationCard = fragmentRoot.findViewById(R.id.sleep_goals_duration_card);
        final View sleepDurationGoalLayout = sleepDurationCard.findViewById(R.id.sleep_goals_duration);
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
        
        View helpClickFrame = sleepDurationCard.findViewById(R.id.sleep_goals_duration_help_click_frame);
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
        DialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("How to hit a wake-time target:")
                    .setView(getLayoutInflater().inflate(R.layout.sleep_goals_help_dialog_waketime, null))
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        
        dialog.show(getChildFragmentManager(), DIALOG_WAKETIME_HELP);
    }
    
    private void displaySleepDurationGoalHelpDialog()
    {
        DialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("How to hit a sleep duration target:")
                    // TODO [21-08-29 6:30PM] -- add help dialog content.
                    .setView(getLayoutInflater().inflate(R.layout.sleep_goals_help_dialog_duration, null))
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
                (dialog, which, hour, minute) -> getViewModel().setSleepDurationGoal(hour, minute));
        durationPickerDialog.show(getChildFragmentManager(), PICKER_SLEEP_DURATION);
    }
    
    private void displaySleepDurationGoalDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        requireContext(),
                        R.string.sleep_goals_delete_duration_dialog_title,
                        (dialog, which) -> getViewModel().clearSleepDurationGoal())
                .show(getChildFragmentManager(), DIALOG_DELETE_DURATION);
        ;
    }
    
    private void displayWakeTimeDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        requireContext(),
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
        
        TimePickerFragment timePicker = new TimePickerFragment();
        timePicker.setArguments(TimePickerFragment.createArguments(cal.get(Calendar.HOUR_OF_DAY),
                                                                   cal.get(Calendar.MINUTE)));
        timePicker.setOnTimeSetListener((view, hourOfDay, minute) -> getViewModel().setWakeTime(
                hourOfDay,
                minute));
        timePicker.show(getChildFragmentManager(), WAKETIME_TIME_PICKER);
    }
}
