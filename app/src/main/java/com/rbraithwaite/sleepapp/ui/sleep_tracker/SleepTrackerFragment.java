package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDirections;
import androidx.navigation.ui.NavigationUI;

import com.rbraithwaite.sleepapp.BuildConfig;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepTrackerFragment
        extends BaseFragment<SleepTrackerFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private SleepTrackerFragmentViewModel mViewModel;
    
    private EditText mAdditionalComments;


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
        return inflater.inflate(R.layout.sleep_tracker_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        initSleepTrackerButton(view);
        initSessionTimeDisplay(view);
        initSessionStartTime(view);
        initGoalsDisplay(view);
        initAdditionalCommentsText(view);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        getViewModel().storeAdditionalComments(mAdditionalComments.getText().toString());
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.sleeptracker_menu, menu);
        
        if (BuildConfig.DEBUG) {
            MenuItem devToolsOption = menu.add("Dev Tools");
            devToolsOption.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    NavDirections toDevTools =
                            SleepTrackerFragmentDirections.actionNavSleeptrackerToDebugNavgraph();
                    getNavController().navigate(toDevTools);
                    return true;
                }
            });
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
    protected boolean getBottomNavVisibility() { return true; }
    
    @Override
    protected Class<SleepTrackerFragmentViewModel> getViewModelClass() { return SleepTrackerFragmentViewModel.class; }
    
//*********************************************************
// private methods
//*********************************************************

    private void initAdditionalCommentsText(View fragmentRoot)
    {
        mAdditionalComments = fragmentRoot.findViewById(R.id.additional_comments);
        getViewModel().getAdditionalComments().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String s)
                    {
                        mAdditionalComments.getText().clear();
                        if (s != null) {
                            mAdditionalComments.getText().append(s);
                        }
                    }
                }
        );
    }

    private void initGoalsDisplay(View fragmentRoot)
    {
        // wake-time goal
        final TextView wakeTimeGoalTitle =
                fragmentRoot.findViewById(R.id.sleep_tracker_waketime_goal_title);
        final TextView wakeTimeGoalValue =
                fragmentRoot.findViewById(R.id.sleep_tracker_waketime_goal_value);
        getViewModel().getWakeTimeGoalText().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String wakeTimeGoalText)
                    {
                        if (wakeTimeGoalText == null) {
                            wakeTimeGoalTitle.setVisibility(View.GONE);
                            wakeTimeGoalValue.setVisibility(View.GONE);
                        } else {
                            wakeTimeGoalTitle.setVisibility(View.VISIBLE);
                            wakeTimeGoalValue.setVisibility(View.VISIBLE);
                            wakeTimeGoalValue.setText(wakeTimeGoalText);
                        }
                    }
                });
        
        // sleep duration goal
        final TextView sleepDurationGoalTitle =
                fragmentRoot.findViewById(R.id.sleep_tracker_duration_goal_title);
        final TextView sleepDurationGoalValue =
                fragmentRoot.findViewById(R.id.sleep_tracker_duration_goal_value);
        getViewModel().getSleepDurationGoalText().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String sleepDurationGoalText)
                    {
                        if (sleepDurationGoalText == null) {
                            sleepDurationGoalTitle.setVisibility(View.GONE);
                            sleepDurationGoalValue.setVisibility(View.GONE);
                        } else {
                            sleepDurationGoalTitle.setVisibility(View.VISIBLE);
                            sleepDurationGoalValue.setVisibility(View.VISIBLE);
                            sleepDurationGoalValue.setText(sleepDurationGoalText);
                        }
                    }
                });
    }
    
    // REFACTOR [20-11-19 3:08AM] -- this shares the inSleepSession LiveData with
    //  initSleepTrackerButton() - consider combining the two into some new method?
    //  maybe bindInSleepSession()??
    //  or consider returning the session start time as LiveData and binding to that instead?
    //      the condition would be on whether the Date value was null or not
    private void initSessionStartTime(View fragmentRoot)
    {
        final TextView startedText = fragmentRoot.findViewById(R.id.sleep_tracker_started_text);
        final TextView sessionStartTime = fragmentRoot.findViewById(R.id.sleep_tracker_start_time);
        final SleepTrackerFragmentViewModel viewModel = getViewModel();
        viewModel.inSleepSession().observe(
                getViewLifecycleOwner(),
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean inSleepSession)
                    {
                        if (inSleepSession) {
                            startedText.setVisibility(View.VISIBLE);
                            sessionStartTime.setVisibility(View.VISIBLE);
                        } else {
                            startedText.setVisibility(View.GONE);
                            sessionStartTime.setVisibility(View.GONE);
                        }
                    }
                }
        );
        viewModel.getSessionStartTime().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String sessionStartTimeString)
                    {
                        sessionStartTime.setText(sessionStartTimeString);
                    }
                }
        );
    }
    
    private void initSessionTimeDisplay(View fragmentRoot)
    {
        final TextView currentSessionTime =
                fragmentRoot.findViewById(R.id.sleep_tracker_session_time);
        getViewModel().getCurrentSleepSessionDuration().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String durationText)
                    {
                        currentSessionTime.setText(durationText);
                    }
                });
    }
    
    private void initSleepTrackerButton(View fragmentRoot)
    {
        final Button sleepTrackerButton = fragmentRoot.findViewById(R.id.sleep_tracker_button);
        
        getViewModel().inSleepSession()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean inSleepSession)
                    {
                        if (inSleepSession) {
                            sleepTrackerButton.setText(R.string.sleep_tracker_button_stop);
                        } else {
                            sleepTrackerButton.setText(R.string.sleep_tracker_button_start);
                        }
                    }
                });
        
        sleepTrackerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SleepTrackerFragmentViewModel viewModel = getViewModel();
                // REFACTOR [21-01-14 12:15AM] -- use LiveDataFuture here to remove the getValue
                //  call.
                Boolean inSleepSession = viewModel.inSleepSession().getValue();
                if (inSleepSession) {
                    viewModel.endSleepSession();
                } else {
                    viewModel.startSleepSession();
                }
            }
        });
    }
    
    // REFACTOR [20-11-15 1:55AM] -- should extract this as a general utility.
    private boolean handleNavigationMenuItem(MenuItem item)
    {
        return NavigationUI.onNavDestinationSelected(item, getNavController())
               || super.onOptionsItemSelected(item);
    }
}
