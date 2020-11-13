package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.rbraithwaite.sleepapp.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SleepTrackerFragment extends Fragment {

    private SleepTrackerFragmentViewModel mViewModel;

    public SleepTrackerFragment() {
        setHasOptionsMenu(true);
    }

//*********************************************************
// Lifecycle callbacks
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sleep_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        final Button sleepTrackerButton = view.findViewById(R.id.sleep_tracker_button);

        getViewModelWithActivity().inSleepSession(requireContext()).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean inSleepSession) {
                if (inSleepSession) {
                    sleepTrackerButton.setText(R.string.sleep_tracker_button_stop);
                } else {
                    sleepTrackerButton.setText(R.string.sleep_tracker_button_start);
                }
            }
        });

        sleepTrackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepTrackerFragmentViewModel viewModel = getViewModelWithActivity();
                Boolean inSleepSession = viewModel.inSleepSession(requireContext()).getValue();
                if (inSleepSession) {
                    viewModel.endSleepSession(requireContext());
                } else {
                    viewModel.startSleepSession(requireContext());
                }
            }
        });
    }

//*********************************************************
// Fragment overrides
//*********************************************************

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.sleeptracker_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
                // handle nav menu items
                NavController navController = Navigation.findNavController(requireActivity(), R.id.main_navhost);
                return NavigationUI.onNavDestinationSelected(item, navController)
                        || super.onOptionsItemSelected(item);
        }
    }

//*********************************************************
// private
//*********************************************************

    private SleepTrackerFragmentViewModel getViewModelWithActivity() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(requireActivity()).get(SleepTrackerFragmentViewModel.class);
        }
        return mViewModel;
    }
}
