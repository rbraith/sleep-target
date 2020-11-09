package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.rbraithwaite.sleepapp.R;

public class SleepTrackerFragment extends Fragment {

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
}
