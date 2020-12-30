package com.rbraithwaite.sleepapp.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragment;
import com.rbraithwaite.sleepapp.ui.session_data.SessionEditData;

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
    
    // REFACTOR [20-12-23 1:39AM] -- consider using the viewmodel to save this UI state data instead
    //  https://developer.android.com/topic/libraries/architecture/saving-states#viewmodel
    // SMELL [20-12-15 3:37AM] -- using a global.
    // This is set when a list item's context menu is opened.
    private int mContextMenuItemPosition;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragment";
    private static final String ADD_SESSION_RESULT = "AddSessionResult";
    private static final String EDIT_SESSION_RESULT = "EditSessionResult";
    private static final String SESSION_DELETE_DIALOG = "SessionDeleteDialog";
    
    private static final int DATA_ICON_DELETE = R.drawable.ic_baseline_delete_forever_24;
    
//*********************************************************
// package properties
//*********************************************************

    // SMELL [20-12-15 3:04AM] -- There should be a better way to do this than having this as a
    //  global, the problem is otherwise in onContextItemSelected() the LiveData instance is liable
    //  to go out of scope.
    LiveData<SessionEditData> mInitialEditData;
    
//*********************************************************
// overrides
//*********************************************************

    // TODO [20-12-24 3:16PM] -- these will need to be the results from
    //  SessionDataFragment.
    @Override // FragmentResultListener
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result)
    {
        // TODO [21-12-29 2:10AM] -- I will need to check for action icon presses here - the action
        //  icon pressed (int) should be returned in the result.
        
        // TODO [20-12-24 3:25PM] -- these will not use SessionEditData result objs
        //  possibly just use SleepSessionData objs? (i want to try to minimize similar POJOs,
        //  or else things get confusing)
        SessionEditData resultData;
        switch (requestKey) {
        case ADD_SESSION_RESULT:
            // REFACTOR [20-12-13 4:15AM] -- should this SessionEditData instantiation be here or
            //  in the viewmodel?
            resultData = SessionEditData.fromResult(result);
            getViewModel().addSessionFromResult(resultData);
            break;
        case EDIT_SESSION_RESULT:
            resultData = SessionEditData.fromResult(result);
            getViewModel().updateSessionFromResult(resultData);
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
                    new SessionArchiveRecyclerViewAdapter.FragmentProvider()
                    {
                        @Override
                        public Fragment getFragment()
                        {
                            return SessionArchiveFragment.this;
                        }
                    },
                    new SessionArchiveRecyclerViewAdapter.OnListItemClickListener()
                    {
                        @Override
                        public void onClick(View v, int position)
                        {
                            // TODO [20-12-24 3:22PM] -- update list item click behaviour here.
//                            mContextMenuItemPosition = position;
//                            v.showContextMenu();
                        }
                    });
        }
        return mRecyclerViewAdapter;
    }
    
//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [20-12-24 3:19PM] -- this should be initSessionDataFragmentResultListeners.
    private void initSessionEditFragmentResultListeners()
    {
        getParentFragmentManager().setFragmentResultListener(ADD_SESSION_RESULT, this, this);
        getParentFragmentManager().setFragmentResultListener(EDIT_SESSION_RESULT, this, this);
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
                Navigation.findNavController(v).navigate(toAddSessionScreen());
            }
        });
    }
    
    /**
     * Generates SafeArgs action for navigating to the add session screen (SessionDataFragment).
     * This is meant to be used in conjunction with NavController.navigate()
     */
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionData toAddSessionScreen()
    {
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionData(
                ADD_SESSION_RESULT,
                getViewModel().getDefaultAddSessionData(),
                SessionDataFragment.DEFAULT_ICON,
                SessionDataFragment.DEFAULT_ICON);
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
