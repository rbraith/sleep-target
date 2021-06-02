package com.rbraithwaite.sleepapp.ui.sleep_goals;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.common.dialog.DialogUtils;
import com.rbraithwaite.sleepapp.ui.common.dialog.DurationPickerFragment;
import com.rbraithwaite.sleepapp.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleepapp.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleepapp.ui.sleep_goals.streak_calendar.StreakCalendar;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;

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
    protected boolean getBottomNavVisibility() { return true; }
    
    @Override
    protected Class<SleepGoalsFragmentViewModel> getViewModelClass() { return SleepGoalsFragmentViewModel.class; }

//*********************************************************
// private methods
//*********************************************************

    private void initStreakCalendar(View fragmentRoot)
    {
        FrameLayout streakCalendarFrame =
                fragmentRoot.findViewById(R.id.sleep_goals_streaks_calendar_frame);
        // use the frame context, so that the theme applied to the frame gets applied to the calendar
        final StreakCalendar streakCalendar = new StreakCalendar(streakCalendarFrame.getContext());
        streakCalendarFrame.addView(streakCalendar.getView());
        
        SleepGoalsFragmentViewModel viewModel = getViewModel();
        LiveData<List<List<Date>>> succeededGoalDates = LiveDataUtils.merge(
                viewModel.getSucceededWakeTimeGoalDates(),
                viewModel.getSucceededSleepDurationGoalDates(),
                (wakeTimeGoalDates, sleepDurationGoalDates) -> Arrays.asList(wakeTimeGoalDates, sleepDurationGoalDates));
        
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
        final View wakeTimeLayout = fragmentRoot.findViewById(R.id.sleep_goals_waketime);
        final Button buttonAddNewWakeTime =
                fragmentRoot.findViewById(R.id.sleep_goals_new_waketime_btn);
        
        // REFACTOR [20-12-23 5:06PM] -- consider just moving the hasWakeTime() logic into
        //  getWakeTime()'s observer (branch on the String being null (no wake time)) inside
        //  initWakeTimeLayout().
        getViewModel().hasWakeTime().observe(
                getViewLifecycleOwner(),
                hasWakeTime -> {
                    if (hasWakeTime != null) {
                        if (hasWakeTime) {
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
    }
    
    // REFACTOR [21-01-29 2:46AM] -- duplicate logic with initWakeTimeGoal()
    private void initSleepDurationGoal(View fragmentRoot)
    {
        final View sleepDurationGoalLayout = fragmentRoot.findViewById(R.id.sleep_goals_duration);
        final Button buttonAddNewSleepDuration =
                fragmentRoot.findViewById(R.id.sleep_goals_new_duration_btn);
        buttonAddNewSleepDuration.setOnClickListener(v -> displaySleepDurationGoalPickerDialog(getViewModel().getDefaultSleepDurationGoal()));
        
        getViewModel().hasSleepDurationGoal().observe(
                getViewLifecycleOwner(),
                hasSleepDurationGoal -> {
                    if (hasSleepDurationGoal != null) {
                        // REFACTOR [21-01-29 2:47AM] -- consider making this:
                        //  toggleSleepDurationGoalDisplay(bool, view, view).
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
    }
    
    private void initSleepDurationGoalLayout(View sleepDurationGoalLayout)
    {
        final TextView valueText =
                sleepDurationGoalLayout.findViewById(R.id.duration_value);
        getViewModel().getSleepDurationGoalText().observe(
                getViewLifecycleOwner(),
                sleepDurationGoalText -> valueText.setText(sleepDurationGoalText));
        
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
        wakeTimeEditButton.setOnClickListener(v -> LiveDataFuture.getValue(
                getViewModel().getWakeTimeGoalDateMillis(),
                getViewLifecycleOwner(),
                // No null check needed since in theory this button should not
                // even be visible unless there is already a wake-time.
                this::displayWakeTimePickerDialog));
        Button wakeTimeDeleteButton = wakeTimeLayout.findViewById(R.id.waketime_delete_btn);
        wakeTimeDeleteButton.setOnClickListener(v -> displayWakeTimeDeleteDialog());
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
        timePicker.setOnTimeSetListener((view, hourOfDay, minute) -> getViewModel().setWakeTime(hourOfDay, minute));
        timePicker.show(getChildFragmentManager(), WAKETIME_TIME_PICKER);
    }
}
