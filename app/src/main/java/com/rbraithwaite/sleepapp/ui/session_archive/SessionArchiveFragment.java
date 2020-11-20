package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends BaseFragment
{
//*********************************************************
// private properties
//*********************************************************

    private SessionArchiveFragmentViewModel mViewModel;
    
    private SessionArchiveRecyclerViewAdapter mRecyclerViewAdapter;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragment";

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
        initRecyclerView(view);
        initFloatingActionButton(view);
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return false; }

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

    private void initFloatingActionButton(View fragmentRoot)
    {
        FloatingActionButton floatingActionButton =
                fragmentRoot.findViewById(R.id.session_archive_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO [20-11-19 9:27PM] -- will be passing data to this eventually
                //  https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args.
                NavDirections toAddSessionFragment =
                        SessionArchiveFragmentDirections.actionSessionArchiveToSessionEdit();
                Navigation.findNavController(v).navigate(toAddSessionFragment);
            }
        });
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
    
    // REFACTOR [20-11-14 5:06PM] -- duplicate code in SleepTrackerFragment
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
