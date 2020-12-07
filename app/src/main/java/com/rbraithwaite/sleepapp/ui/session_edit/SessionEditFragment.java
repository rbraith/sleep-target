package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.dialog.DatePickerFragment;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionEditFragment
        extends BaseFragment<SessionEditFragmentViewModel>
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionEditFragment";
    
    private static final String DIALOG_START_DATE_PICKER = "StartDatePicker";
    private static final String DIALOG_START_TIME_PICKER = "StartTimePicker";

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
// api
//*********************************************************

    public static Bundle createArguments(long startDateMillis, long endDateMillis)
    {
        // use SafeArgs action so that the Bundle works when it is eventually used with
        // SessionEditFragmentArgs.fromBundle()
        // TODO [20-11-28 10:30PM] -- SafeArgs uses the argument names defined in the navgraph as
        //  the Bundle keys - consider redefining those keys here and just making my own Bundle?
        //  problem: the argument names would be hardcoded though, I can't seem to find a way to
        //  get a reference to the names defined in the navgraph, but I should investigate more.
        return SessionArchiveFragmentDirections
                .actionSessionArchiveToSessionEdit(startDateMillis, endDateMillis)
                .getArguments();
    }

//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [20-12-1 2:09AM] -- the params should be start & end time millis.
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
        
        final TextView startTime = startTimeLayout.findViewById(R.id.time);
        final TextView startDate = startTimeLayout.findViewById(R.id.date);
        
        // set up dialog listeners
        final SessionEditFragmentViewModel viewModel = getViewModel();
        startDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(DatePickerFragment.createArguments(
                        viewModel.getStartDateTime().getValue(),
                        null,
                        viewModel.getEndDateTime().getValue()));
                datePicker.setOnDateSetListener(new DatePickerFragment.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        viewModel.setStartDate(year, month, dayOfMonth);
                    }
                });
                datePicker.show(getChildFragmentManager(), DIALOG_START_DATE_PICKER);
            }
        });
        
        startTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimePickerFragment timePicker = new TimePickerFragment();
                // IDEA [20-12-5 8:36PM] -- consider creating a custom TimePickerDialog which has
                //  max/min times (instead of allowing user to pick any time)
                //  https://stackoverflow.com/a/16942630
                //  I decided not to go with this idea for now since I would need to find a way
                //  to grey-out un-selectable times in order to match the behaviour of
                //  DatePicker.maxDate(), minDate()
                //  --
                //  Note: If I were to go with this behaviour, I would need to rework
                //  DatePickerFragment
                //  to use max/min date values (as it originally did - see DatePickerFragment &
                //  SessionEditFragment.initStartTime() in commit [main c3d7e12])
                timePicker.setArguments(TimePickerFragment.createArguments(viewModel.getStartDateTime()
                                                                                   .getValue()));
                timePicker.show(getChildFragmentManager(), DIALOG_START_TIME_PICKER);
            }
        });
        
        // bind input fields
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
