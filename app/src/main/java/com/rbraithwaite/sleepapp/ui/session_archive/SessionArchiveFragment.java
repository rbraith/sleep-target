package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rbraithwaite.sleepapp.MainActivity;
import com.rbraithwaite.sleepapp.R;

public class SessionArchiveFragment extends Fragment {

//*********************************************************
// Lifecycle callbacks
//*********************************************************
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.session_archive_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setMainActivityBottomNavVisibility(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setMainActivityBottomNavVisibility(true);
    }

//*********************************************************
// private
//*********************************************************

    private void setMainActivityBottomNavVisibility(boolean visibility) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setBottomNavVisibility(visibility);
        }
    }
}
