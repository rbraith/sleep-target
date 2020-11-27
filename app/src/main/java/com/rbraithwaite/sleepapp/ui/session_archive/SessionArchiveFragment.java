package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends BaseFragment<SessionArchiveFragmentViewModel>
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
    
    @Override
    protected Class<SessionArchiveFragmentViewModel> getViewModelClass() { return SessionArchiveFragmentViewModel.class; }
    
//*********************************************************
// api
//*********************************************************

    public SessionArchiveRecyclerViewAdapter getRecyclerViewAdapter()
    {
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new SessionArchiveRecyclerViewAdapter(
                    getViewModel(),
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
                Navigation.findNavController(v).navigate(toAddSessionFragment());
            }
        });
    }
    
    /**
     * Generates SafeArgs action for navigating to the AddSessionFragment. This is meant to be used
     * in conjunction with NavController.navigate()
     */
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionEdit toAddSessionFragment()
    {
        long defaultDateTime = getViewModel().getDefaultAddSessionFragmentDateTime();
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionEdit(
                defaultDateTime, defaultDateTime);
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
