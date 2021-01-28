package com.rbraithwaite.sleepapp.ui.session_data;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.dialog.DatePickerFragment;
import com.rbraithwaite.sleepapp.ui.dialog.DialogUtils;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.io.Serializable;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SessionDataFragment
        extends BaseFragment<SessionDataFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private int mPositiveIcon;
    private int mNegativeIcon;
    private ActionListener mPositiveActionListener;
    private ActionListener mNegativeActionListener;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionDataFragment";
    
    private static final String DIALOG_START_DATE_PICKER = "StartDatePicker";
    private static final String DIALOG_START_TIME_PICKER = "StartTimePicker";
    private static final String DIALOG_END_DATE_PICKER = "EndDatePicker";
    private static final String DIALOG_END_TIME_PICKER = "EndTimePicker";
    private static final String DIALOG_WAKETIME_TIME_PICKER = "WakeTimePicker";
    private static final String DIALOG_DELETE_WAKETIME = "DeleteWakeTimeGoal";
    
//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_ICON = -1;

//*********************************************************
// public helpers
//*********************************************************
    
    public static class Args
            implements Serializable
    {
        public static final long serialVersionUID = 20201230L;
        
        public SleepSessionWrapper initialData;
        public ActionListener positiveActionListener;
        public ActionListener negativeActionListener;
        public int positiveIcon = DEFAULT_ICON;
        public int negativeIcon = DEFAULT_ICON;
    }
    
    public static class ArgsBuilder
    {
        private Args mArgs;
        
        public ArgsBuilder(SleepSessionWrapper initialData)
        {
            mArgs = new Args();
            mArgs.initialData = initialData;
        }
        
        public ArgsBuilder setPositiveActionListener(ActionListener positiveActionListener)
        {
            mArgs.positiveActionListener = positiveActionListener;
            return this;
        }
        
        public ArgsBuilder setNegativeActionListener(ActionListener negativeActionListener)
        {
            mArgs.negativeActionListener = negativeActionListener;
            return this;
        }
        
        public ArgsBuilder setPositiveIcon(int positiveIcon)
        {
            mArgs.positiveIcon = positiveIcon;
            return this;
        }
        
        public ArgsBuilder setNegativeIcon(int negativeIcon)
        {
            mArgs.negativeIcon = negativeIcon;
            return this;
        }
        
        public Args build() {return mArgs;}
    }
    
    public static abstract class ActionListener
            implements Serializable
    {
        public static final long serialVersionUID = 20201230L;
        
        /**
         * The fragment is passed so that clients can control whether or not the fragment is
         * completed, among other things.
         */
        public abstract void onAction(SessionDataFragment fragment, SleepSessionWrapper result);
    }

//*********************************************************
// constructors
//*********************************************************

    public SessionDataFragment() { setHasOptionsMenu(true); }
    
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
        return inflater.inflate(R.layout.session_data_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        SessionDataFragmentArgs safeArgs = SessionDataFragmentArgs.fromBundle(getArguments());
        Args args = safeArgs.getArgs();
        // init members
        mPositiveIcon = args.positiveIcon;
        mNegativeIcon = args.negativeIcon;
        mPositiveActionListener = args.positiveActionListener;
        mNegativeActionListener = args.negativeActionListener;
        
        // init view model
        getViewModel().initSessionData(args.initialData);
        
        // init views
        initStartDateTime(view.findViewById(R.id.session_data_start_time));
        initEndDateTime(view.findViewById(R.id.session_data_end_time));
        initSessionDuration(view);
        initWakeTimeGoal(view);
        
        // init back press behaviour
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true)
                {
                    @Override
                    public void handleOnBackPressed()
                    {
                        clearSessionDataThenNavigateUp();
                    }
                });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.session_data_menu, menu);
        if (mPositiveIcon != DEFAULT_ICON) {
            menu.findItem(R.id.session_data_action_positive).setIcon(mPositiveIcon);
        }
        if (mNegativeIcon != DEFAULT_ICON) {
            menu.findItem(R.id.session_data_action_negative).setIcon(mNegativeIcon);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // The viewmodel session is cleared here to distinguish cases where the fragment is
        // destroyed on a user action vs by the system (eg an orientation change, where the session
        // should be preserved and the UI re-initialized)
        switch (item.getItemId()) {
        case android.R.id.home: // up button
            clearSessionDataThenNavigateUp();
            return true;
        case R.id.session_data_action_negative:
            if (mNegativeActionListener != null) {
                mNegativeActionListener.onAction(this, getViewModel().getResult());
            } else {
                clearSessionDataThenNavigateUp();
            }
            return true;
        case R.id.session_data_action_positive:
            // REFACTOR [20-12-16 5:56PM] -- should getResult be returning
            //  LiveData<SleepSessionData>? should the implementation be a transformation?
            //  leaving this for now since things seem to be working.
            if (mPositiveActionListener != null) {
                mPositiveActionListener.onAction(this, getViewModel().getResult());
            } else {
                clearSessionDataThenNavigateUp();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return false; }

    @Override
    protected Class<SessionDataFragmentViewModel> getViewModelClass() { return SessionDataFragmentViewModel.class; }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(Args args)
    {
        // use SafeArgs action so that the Bundle works when it is eventually used with
        // SessionDataFragmentArgs.fromBundle()
        // REFACTOR [20-11-28 10:30PM] -- SafeArgs uses the argument names defined in the
        //  navgraph as the Bundle keys - consider redefining those keys here and just making my
        //  own Bundle? problem: the argument names would be hardcoded though, I can't seem to find
        //  a way to get a reference to the names defined in the navgraph, but I should
        //  investigate more.
        return SessionArchiveFragmentDirections
                .actionSessionArchiveToSessionData(args)
                .getArguments();
    }

    // TODO [21-12-31 1:54AM] -- I should think more about possible ways of unit testing this.
    public void completed()
    {
        clearSessionDataThenNavigateUp();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void clearSessionDataThenNavigateUp()
    {
        getViewModel().clearSessionData();
        Navigation.findNavController(getView()).navigateUp();
    }
    
    private void initWakeTimeGoal(final View fragmentRoot)
    {
        // REFACTOR [21-01-27 11:28PM] -- maybe search from wakeTimeLayout instead?
        final TextView wakeTimeText = fragmentRoot.findViewById(R.id.session_data_goal_waketime);
        wakeTimeText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayWakeTimeGoalDialog();
            }
        });
        final Button addWakeTimeButton =
                fragmentRoot.findViewById(R.id.session_data_add_waketime_btn);
        addWakeTimeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayWakeTimeGoalDialog();
            }
        });
        final ImageButton deleteWakeTimeButton =
                fragmentRoot.findViewById(R.id.session_data_delete_waketime_btn);
        deleteWakeTimeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialogFragment deleteDialog = DialogUtils.createDeleteDialog(
                        requireContext(),
                        R.string.session_data_delete_waketime_dialog_title,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                getViewModel().clearWakeTimeGoal();
                            }
                        });
                
                // REFACTOR [21-01-28 12:34AM] -- should be just getChildFragmentManager() oops
                deleteDialog.show(SessionDataFragment.this.getChildFragmentManager(),
                                  DIALOG_DELETE_WAKETIME);
            }
        });
        final View wakeTimeLayout = fragmentRoot.findViewById(R.id.session_data_waketime_layout);
        getViewModel().getWakeTimeGoal().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String wakeTimeGoal)
                    {
                        // REFACTOR [21-01-15 8:32PM] -- consider using a WakeTimeGoalModel with an
                        //  unset state, instead of using null.
                        if (wakeTimeGoal == null) {
                            addWakeTimeButton.setVisibility(View.VISIBLE);
                            wakeTimeLayout.setVisibility(View.GONE);
                        } else {
                            addWakeTimeButton.setVisibility(View.GONE);
                            wakeTimeLayout.setVisibility(View.VISIBLE);
                            wakeTimeText.setText(wakeTimeGoal);
                        }
                    }
                });
    }
    
    private void displayWakeTimeGoalDialog()
    {
        final SessionDataFragmentViewModel viewModel = getViewModel();
        final TimePickerFragment timePicker = new TimePickerFragment();
        
        // REFACTOR [21-01-15 11:06PM] -- consider instead adding an observer during the
        //  initialization of the fragment, which just updates an mWakeTimeGoalMillis property.
        // This LiveDataFuture is needed to activate getWakeTimeGoalMillis() (otherwise it is
        // always null).
        LiveDataFuture.getValue(
                viewModel.getWakeTimeGoalMillis(),
                getViewLifecycleOwner(),
                new LiveDataFuture.OnValueListener<Long>()
                {
                    @Override
                    public void onValue(Long wakeTimeGoalMillis)
                    {
                        long defaultValue =
                                wakeTimeGoalMillis == null ?
                                        viewModel.getDefaultWakeTimeGoalMillis() :
                                        wakeTimeGoalMillis;
                        
                        timePicker.setArguments(TimePickerFragment.createArguments(defaultValue));
                        timePicker.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                viewModel.setWakeTimeGoal(hourOfDay, minute);
                            }
                        });
                        timePicker.show(getChildFragmentManager(), DIALOG_WAKETIME_TIME_PICKER);
                    }
                }
        );
    }
    
    private void initSessionDuration(View fragmentRoot)
    {
        final TextView sessionDurationText = fragmentRoot.findViewById(R.id.session_data_duration);
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
    
    private void initStartDateTime(View startTimeLayout)
    {
        // init label
        TextView startTimeName = startTimeLayout.findViewById(R.id.name);
        startTimeName.setText(R.string.session_data_start_time_name);
        
        final TextView startTime = startTimeLayout.findViewById(R.id.time);
        final TextView startDate = startTimeLayout.findViewById(R.id.date);
        
        initStartDateTimeListeners(startDate, startTime);
        
        bindStartDateTimeViews(startDate, startTime);
    }
    
    private void bindStartDateTimeViews(final TextView startDateText, final TextView startTimeText)
    {
        getViewModel().getStartTime().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newStartTime)
            {
                startTimeText.setText(newStartTime);
            }
        });
        getViewModel().getStartDate().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newStartDate)
            {
                startDateText.setText(newStartDate);
            }
        });
    }
    
    private void initStartDateTimeListeners(TextView startDateText, TextView startTimeText)
    {
        final SessionDataFragmentViewModel viewModel = getViewModel();
        startDateText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(DatePickerFragment.createArguments(
                        viewModel.getStartDateTime().getValue()));
                datePicker.setOnDateSetListener(new DatePickerFragment.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        try {
                            viewModel.setStartDate(year, month, dayOfMonth);
                        } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                            Snackbar.make(getView(),
                                          R.string.error_session_edit_start_datetime,
                                          Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                datePicker.show(getChildFragmentManager(), DIALOG_START_DATE_PICKER);
            }
        });
        
        startTimeText.setOnClickListener(new View.OnClickListener()
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
                timePicker.setArguments(TimePickerFragment.createArguments(
                        viewModel.getStartDateTime().getValue()));
                timePicker.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        try {
                            viewModel.setStartTime(hourOfDay, minute);
                        } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                            Log.d(TAG, "onTimeSet: invalid start time");
                            Snackbar.make(getView(),
                                          R.string.error_session_edit_start_datetime,
                                          Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                timePicker.show(getChildFragmentManager(), DIALOG_START_TIME_PICKER);
            }
        });
    }
    
    private void initEndDateTime(View endTimeLayout)
    {
        // init label
        TextView endTimeName = endTimeLayout.findViewById(R.id.name);
        endTimeName.setText(R.string.session_data_end_time_name);
        
        final TextView endTime = endTimeLayout.findViewById(R.id.time);
        final TextView endDate = endTimeLayout.findViewById(R.id.date);
        
        initEndDateTimeListeners(endDate, endTime);
        
        bindEndDateTimeViews(endDate, endTime);
    }
    
    private void initEndDateTimeListeners(final TextView endDateText, final TextView endTimeText)
    {
        final SessionDataFragmentViewModel viewModel = getViewModel();
        endDateText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(DatePickerFragment.createArguments(
                        viewModel.getEndDateTime().getValue()));
                datePicker.setOnDateSetListener(new DatePickerFragment.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        try {
                            viewModel.setEndDate(year, month, dayOfMonth);
                        } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                            Snackbar.make(getView(),
                                          R.string.error_session_edit_end_datetime,
                                          Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                datePicker.show(getChildFragmentManager(), DIALOG_END_DATE_PICKER);
            }
        });
        endTimeText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimePickerFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(TimePickerFragment.createArguments(
                        viewModel.getEndDateTime().getValue()));
                timePicker.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        try {
                            viewModel.setEndTime(hourOfDay, minute);
                        } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                            Log.d(TAG, "onTimeSet: invalid end time");
                            Snackbar.make(getView(),
                                          R.string.error_session_edit_end_datetime,
                                          Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                timePicker.show(getChildFragmentManager(), DIALOG_END_TIME_PICKER);
            }
        });
    }
    
    private void bindEndDateTimeViews(final TextView endDateText, final TextView endTimeText)
    {
        getViewModel().getEndTime().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newEndTime)
            {
                endTimeText.setText(newEndTime);
            }
        });
        getViewModel().getEndDate().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(String newEndDate)
            {
                endDateText.setText(newEndDate);
            }
        });
    }
}
