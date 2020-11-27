package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionEditFragment
        extends BaseFragment<SessionEditFragmentViewModel>
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionEditFragment";

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
        return inflater.inflate(R.layout.session_edit_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        initStartTime(view.findViewById(R.id.session_edit_start_time));
        initEndTime(view.findViewById(R.id.session_edit_end_time));
        initSessionDuration(view);
        
        SessionEditFragmentArgs args = SessionEditFragmentArgs.fromBundle(getArguments());
        initInputFieldValues(args);
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return false; }
    
    @Override
    protected Class<SessionEditFragmentViewModel> getViewModelClass() { return SessionEditFragmentViewModel.class; }
    
//*********************************************************
// private methods
//*********************************************************

    private void initInputFieldValues(SessionEditFragmentArgs args)
    {
        getViewModel().setStartDateTime(args.getStartTime());
        getViewModel().setEndDateTime(args.getEndTime());
    }
    
    private void initSessionDuration(View fragmentRoot)
    {
        final TextView sessionDurationText = fragmentRoot.findViewById(R.id.session_edit_duration);
        getViewModel().getSessionDuration().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String newSessionDurationText)
                    {
                        sessionDurationText.setText(newSessionDurationText);
                    }
                });
    }
    
    private void initStartTime(View startTimeLayout)
    {
        // init label
        TextView startTimeName = startTimeLayout.findViewById(R.id.name);
        startTimeName.setText(R.string.session_edit_start_time_name);
        
        // bind input fields
        final TextView startTime = startTimeLayout.findViewById(R.id.time);
        final TextView startDate = startTimeLayout.findViewById(R.id.date);
        
        getViewModel().getStartTime().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newStartTime)
            {
                startTime.setText(newStartTime);
            }
        });
        getViewModel().getStartDate().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newStartDate)
            {
                startDate.setText(newStartDate);
            }
        });
    }
    
    private void initEndTime(View endTimeLayout)
    {
        // init label
        TextView endTimeName = endTimeLayout.findViewById(R.id.name);
        endTimeName.setText(R.string.session_edit_end_time_name);
        
        // bind input fields
        final TextView endTime = endTimeLayout.findViewById(R.id.time);
        final TextView endDate = endTimeLayout.findViewById(R.id.date);
        
        getViewModel().getEndTime().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newEndTime)
            {
                endTime.setText(newEndTime);
            }
        });
        getViewModel().getEndDate().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newEndDate)
            {
                endDate.setText(newEndDate);
            }
        });
    }
}
