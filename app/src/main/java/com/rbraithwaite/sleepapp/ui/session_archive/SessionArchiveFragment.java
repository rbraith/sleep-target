package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends Fragment
{
//*********************************************************
// private properties
//*********************************************************

    private SessionArchiveFragmentViewModel mViewModel;
    
    private SessionArchiveRecyclerViewAdapter mRecyclerViewAdapter;
    
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
        return inflater.inflate(R.layout.session_archive_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        RecyclerView recyclerView = view.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setMainActivityBottomNavVisibility(false);
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        
        setMainActivityBottomNavVisibility(true);
    }

    
//*********************************************************
// api
//*********************************************************

    public SessionArchiveRecyclerViewAdapter getRecyclerViewAdapter()
    {
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new SessionArchiveRecyclerViewAdapter(
                    getViewModelWithActivity(),
                    new SessionArchiveRecyclerViewAdapter.LifeCycleOwnerProvider()
                    {
                        @Override
                        public LifecycleOwner getLifeCycleOwner()
                        {
                            return SessionArchiveFragment.this;
                        }
                    });
        }
        return mRecyclerViewAdapter;
    }

    
//*********************************************************
// private methods
//*********************************************************

    private void setMainActivityBottomNavVisibility(boolean visibility)
    {
        FragmentActivity activity = getActivity();
        // its possible this fragment will not be inside a MainActivity (eg it could
        // be inside a test-specific activity)
        // TODO type check (code smell)
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setBottomNavVisibility(visibility);
        }
    }
    
    // TODO
    //  duplicate code in SleepTrackerFragment
    //  consider making a generic base fragment w/ common viewmodel ops
    private SessionArchiveFragmentViewModel getViewModelWithActivity()
    {
        if (mViewModel == null) {
            mViewModel =
                    new ViewModelProvider(requireActivity()).get(SessionArchiveFragmentViewModel.class);
        }
        return mViewModel;
    }
}
