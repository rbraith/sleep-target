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
import com.rbraithwaite.sleepapp.ui.dialog.DialogUtils;
import com.rbraithwaite.sleepapp.ui.dialog.DurationPickerFragment;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;
import com.rbraithwaite.sleepapp.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleepapp.ui.sleep_goals.streak_calendar.StreakCalendar;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;

import java.util.Arrays;
import java.util.Date;
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
        final StreakCalendar streakCalendar = new StreakCalendar(requireContext());
        FrameLayout streakCalendarFrame =
                fragmentRoot.findViewById(R.id.sleep_goals_streaks_calendar_frame);
        streakCalendarFrame.addView(streakCalendar.getView());
        
        SleepGoalsFragmentViewModel viewModel = getViewModel();
        LiveData<List<List<Date>>> succeededGoalDates = LiveDataUtils.merge(
                viewModel.getSucceededWakeTimeGoalDates(),
                viewModel.getSucceededSleepDurationGoalDates(),
                new LiveDataUtils.Merger<List<Date>, List<Date>, List<List<Date>>>()
                {
                    @Override
                    public List<List<Date>> applyMerge(
                            List<Date> wakeTimeGoalDates,
                            List<Date> sleepDurationGoalDates)
                    {
                        return Arrays.asList(wakeTimeGoalDates, sleepDurationGoalDates);
                    }
                });
        
        // OPTIMIZE [21-03-14 10:44PM] -- right now I am using all succeeded goal dates in history -
        //  it would probably be better to only use those relevant to the currently displayed month
        //  of the calendar
        //  ---
        //  I would need new interfaces in the view model for finding succeeded goal dates within a
        //  range, and I would need a callback on the streak calendar, notifying when the user
        //  changes to a new month.
        succeededGoalDates.observe(
                getViewLifecycleOwner(),
                new Observer<List<List<Date>>>()
                {
                    @Override
                    public void onChanged(List<List<Date>> bothGoalDates)
                    {
                        List<Date> wakeTimeGoalDates = bothGoalDates.get(0);
                        List<Date> sleepDurationGoalDates = bothGoalDates.get(1);
                        
                        streakCalendar.setSucceededGoalDates(wakeTimeGoalDates,
                                                             sleepDurationGoalDates);
                    }
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
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean hasWakeTime)
                    {
                        if (hasWakeTime != null) {
                            if (hasWakeTime) {
                                wakeTimeLayout.setVisibility(View.VISIBLE);
                                buttonAddNewWakeTime.setVisibility(View.GONE);
                            } else {
                                buttonAddNewWakeTime.setVisibility(View.VISIBLE);
                                wakeTimeLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                });
        
        buttonAddNewWakeTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayWakeTimePickerDialog(getViewModel().getDefaultWakeTime());
            }
        });
        
        initWakeTimeLayout(wakeTimeLayout);
    }
    
    // REFACTOR [21-01-29 2:46AM] -- duplicate logic with initWakeTimeGoal()
    private void initSleepDurationGoal(View fragmentRoot)
    {
        final View sleepDurationGoalLayout = fragmentRoot.findViewById(R.id.sleep_goals_duration);
        final Button buttonAddNewSleepDuration =
                fragmentRoot.findViewById(R.id.sleep_goals_new_duration_btn);
        buttonAddNewSleepDuration.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displaySleepDurationGoalPickerDialog(getViewModel().getDefaultSleepDurationGoal());
            }
        });
        
        getViewModel().hasSleepDurationGoal().observe(
                getViewLifecycleOwner(),
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean hasSleepDurationGoal)
                    {
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
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String sleepDurationGoal)
                    {
                        valueText.setText(sleepDurationGoal);
                    }
                });
        
        Button editButton = sleepDurationGoalLayout.findViewById(R.id.duration_edit_btn);
        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LiveDataFuture.getValue(
                        getViewModel().getSleepDurationGoal(),
                        getViewLifecycleOwner(),
                        new LiveDataFuture.OnValueListener<SleepDurationGoalUIData>()
                        {
                            @Override
                            public void onValue(SleepDurationGoalUIData goal)
                            {
                                displaySleepDurationGoalPickerDialog(goal);
                            }
                        });
            }
        });
        
        Button deleteButton = sleepDurationGoalLayout.findViewById(R.id.duration_delete_btn);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displaySleepDurationGoalDeleteDialog();
            }
        });
    }
    
    
    private void initWakeTimeLayout(View wakeTimeLayout)
    {
        final TextView wakeTimeValue = wakeTimeLayout.findViewById(R.id.waketime_value);
        getViewModel().getWakeTimeText().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String waketime)
                    {
                        wakeTimeValue.setText(waketime);
                    }
                });
        Button wakeTimeEditButton = wakeTimeLayout.findViewById(R.id.waketime_edit_btn);
        wakeTimeEditButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LiveDataFuture.getValue(
                        getViewModel().getWakeTimeGoalDateMillis(),
                        getViewLifecycleOwner(),
                        new LiveDataFuture.OnValueListener<Long>()
                        {
                            @Override
                            public void onValue(Long wakeTimeDateMillis)
                            {
                                // No null check needed since in theory this button should not
                                // even be visible unless there is already a wake-time.
                                displayWakeTimePickerDialog(wakeTimeDateMillis);
                            }
                        });
            }
        });
        Button wakeTimeDeleteButton = wakeTimeLayout.findViewById(R.id.waketime_delete_btn);
        wakeTimeDeleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayWakeTimeDeleteDialog();
            }
        });
    }
    
    private void displaySleepDurationGoalPickerDialog(SleepDurationGoalUIData initialValue)
    {
        DurationPickerFragment durationPickerDialog = DurationPickerFragment.createInstance(
                initialValue.hours,
                initialValue.remainingMinutes,
                new DurationPickerFragment.OnDurationSetListener()
                {
                    @Override
                    public void onDurationSet(
                            DialogInterface dialog,
                            int which,
                            int hour,
                            int minute)
                    {
                        getViewModel().setSleepDurationGoal(hour, minute);
                    }
                });
        durationPickerDialog.show(getChildFragmentManager(), PICKER_SLEEP_DURATION);
    }
    
    private void displaySleepDurationGoalDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        requireContext(),
                        R.string.sleep_goals_delete_duration_dialog_title,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                getViewModel().clearSleepDurationGoal();
                            }
                        })
                .show(getChildFragmentManager(), DIALOG_DELETE_DURATION);
        ;
    }
    
    private void displayWakeTimeDeleteDialog()
    {
        DialogUtils
                .createDeleteDialog(
                        requireContext(),
                        R.string.sleep_goals_delete_waketime_dialog_title,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                getViewModel().clearWakeTime();
                            }
                        })
                .show(getChildFragmentManager(), DIALOG_DELETE_WAKETIME);
    }
    
    private void displayWakeTimePickerDialog(long defaultValueDateMillis)
    {
        TimePickerFragment timePicker = new TimePickerFragment();
        // SMELL [20-12-21 10:39PM] -- since the time picker is relative, it doesn't make
        //  much sense passing an absolute datetime (even though this was convenient in
        //  SessionEditFragment, ie using the same datetime for the date picker & time
        //  picker)
        //  ---
        //  consider passing direct hour & minute values instead.
        timePicker.setArguments(TimePickerFragment.createArguments(defaultValueDateMillis));
        timePicker.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
            {
                getViewModel().setWakeTime(hourOfDay, minute);
            }
        });
        timePicker.show(getChildFragmentManager(), WAKETIME_TIME_PICKER);
    }
}
