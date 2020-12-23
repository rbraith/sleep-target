package com.rbraithwaite.sleepapp.ui.sleep_goals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepGoalsFragment
        extends BaseFragment<SleepGoalsFragmentViewModel>
{
//*********************************************************
// private constants
//*********************************************************

    private static final String WAKETIME_TIME_PICKER = "WakeTimeTimePicker";

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
        final View wakeTime = view.findViewById(R.id.sleep_goals_waketime);
        final Button buttonAddNewWakeTime = view.findViewById(R.id.sleep_goals_new_waketime_btn);
        
        getViewModel().hasWakeTime().observe(
                getViewLifecycleOwner(),
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean hasWakeTime)
                    {
                        if (hasWakeTime != null) {
                            if (hasWakeTime) {
                                wakeTime.setVisibility(View.VISIBLE);
                                buttonAddNewWakeTime.setVisibility(View.GONE);
                            } else {
                                buttonAddNewWakeTime.setVisibility(View.VISIBLE);
                                wakeTime.setVisibility(View.GONE);
                            }
                        }
                    }
                });
        
        final TextView wakeTimeValue = view.findViewById(R.id.waketime_value);
        getViewModel().getWakeTime().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String waketime)
                    {
                        wakeTimeValue.setText(waketime);
                    }
                });
        
        buttonAddNewWakeTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimePickerFragment timePicker = new TimePickerFragment();
                // SMELL [20-12-21 10:39PM] -- since the time picker is relative, it doesn't make
                //  much sense passing an absolute datetime (even though this was convenient in
                //  SessionEditFragment, ie using the same datetime for the date picker & time
                //  picker)
                //  ---
                //  consider passing direct hour & minute values instead.
                timePicker.setArguments(TimePickerFragment.createArguments(getViewModel().getDefaultWakeTime()));
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
        });
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return true; }
    
    @Override
    protected Class<SleepGoalsFragmentViewModel> getViewModelClass() { return SleepGoalsFragmentViewModel.class; }
}
