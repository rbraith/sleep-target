package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends BaseFragment<SessionArchiveFragmentViewModel>
        implements FragmentResultListener
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
    
    private static final String ADD_SESSION_RESULT = "AddSessionResult";
    private static final String EDIT_SESSION_RESULT = "EditSessionResult";

//*********************************************************
// overrides
//*********************************************************

    @Override // FragmentResultListener
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result)
    {
        switch (requestKey) {
        case ADD_SESSION_RESULT:
            // REFACTOR [20-12-13 4:15AM] -- should this SessionEditData instantiation be here or
            //  in the viewmodel?
            SessionEditData resultData = SessionEditData.fromResult(result);
            getViewModel().addSessionFromResult(resultData);
            break;
        }
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initSessionEditFragmentResultListeners();
    }
    
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

    private void initSessionEditFragmentResultListeners()
    {
        getParentFragmentManager().setFragmentResultListener(ADD_SESSION_RESULT, this, this);
        // TODO [20-12-12 10:26PM] -- EDIT_SESSION_RESULT.
    }
    
    
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
                ADD_SESSION_RESULT, new SessionEditData(defaultDateTime, defaultDateTime));
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
