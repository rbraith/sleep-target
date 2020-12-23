package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

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
        return inflater.inflate(R.layout.sleep_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        initSleepTrackerButton(view);
        initSessionTimeDisplay(view);
        initSessionStartTime(view);
        initGoalsDisplay(view);
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.sleeptracker_menu, menu);
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

    private void initGoalsDisplay(View fragmentRoot)
    {
        final TextView wakeTimeText = fragmentRoot.findViewById(R.id.sleep_tracker_waketime);
        getViewModel().getWakeTime().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String waketime)
                    {
                        if (waketime == null) {
                            wakeTimeText.setVisibility(View.GONE);
                        } else {
                            wakeTimeText.setVisibility(View.VISIBLE);
                            wakeTimeText.setText("Wake-Time Goal: " + waketime);
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
        final Context context = requireContext();
        viewModel.inSleepSession(context).observe(
                getViewLifecycleOwner(),
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(Boolean inSleepSession)
                    {
                        if (inSleepSession) {
                            startedText.setVisibility(View.VISIBLE);
                            sessionStartTime.setVisibility(View.VISIBLE);
                            sessionStartTime.setText(viewModel.getSessionStartTime(context));
                        } else {
                            startedText.setVisibility(View.GONE);
                            sessionStartTime.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }
    
    private void initSessionTimeDisplay(View fragmentRoot)
    {
        final TextView currentSessionTime =
                fragmentRoot.findViewById(R.id.sleep_tracker_session_time);
        getViewModel().getCurrentSleepSessionDuration(requireContext()).observe(
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
        
        getViewModel().inSleepSession(requireContext())
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
                Boolean inSleepSession = viewModel.inSleepSession(requireContext()).getValue();
                if (inSleepSession) {
                    viewModel.endSleepSession(requireContext());
                } else {
                    viewModel.startSleepSession(requireContext());
                }
            }
        });
    }
    
    // REFACTOR [20-11-15 1:55AM] -- should extract this as a general utility.
    private boolean handleNavigationMenuItem(MenuItem item)
    {
        NavController navController =
                Navigation.findNavController(requireActivity(), R.id.main_navhost);
        return NavigationUI.onNavDestinationSelected(item, navController)
               || super.onOptionsItemSelected(item);
    }
}
