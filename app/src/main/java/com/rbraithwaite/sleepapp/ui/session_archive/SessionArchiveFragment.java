package com.rbraithwaite.sleepapp.ui.session_archive;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

    @Override // FragmentResultListener
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result)
    {
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
    public void onCreateContextMenu(
            @NonNull ContextMenu menu,
            @NonNull View v,
            @Nullable ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        requireActivity().getMenuInflater()
                .inflate(R.menu.session_archive_list_item_context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.session_archive_list_item_context_menu_EDIT:
            Log.d(TAG, "onContextItemSelected: editing!!!");
            int selectedSessionId = getViewModel().getAllSleepSessionDataIds()
                    .getValue()
                    .get(mContextMenuItemPosition);
            mInitialEditData = getViewModel().getInitialEditSessionData(selectedSessionId);
            // REFACTOR [20-12-15 3:45AM] -- consider just using observeForever, originally I used
            //  observe(LifecycleOwner, ...) as an additional safety net (idk if that even makes
            //  sense though).
            mInitialEditData.observe(getViewLifecycleOwner(), new Observer<SessionEditData>()
            {
                @Override
                public void onChanged(SessionEditData initialEditData)
                {
                    if (initialEditData != null) {
                        Navigation.findNavController(getView())
                                .navigate(toEditSessionScreen(initialEditData));
                        // REFACTOR [20-12-17 9:08PM] -- consider making a OneTimeObserver utility.
                        mInitialEditData.removeObserver(this); // one-off observer
                    }
                }
            });
            return true;
        case R.id.session_archive_list_item_context_menu_DELETE:
            SessionArchiveDeleteDialog deleteDialog = SessionArchiveDeleteDialog.createInstance(
                    SessionArchiveDeleteDialog.createArguments(mContextMenuItemPosition),
                    new SessionArchiveDeleteDialog.OnPositiveButtonClickListener()
                    {
                        @Override
                        public void onPositiveButtonClick(
                                DialogInterface dialog,
                                int sessionPosition)
                        {
                            Log.d(TAG,
                                  "onPositiveButtonClick: deleting session " + sessionPosition +
                                  "!!");
                            // REFACTOR [20-12-17 8:38PM] -- should this be viewModel
                            //  .getSleepSessionDataIdFromPosition()?
                            // REFACTOR [20-12-17 8:39PM] -- Is using getValue() bad here (and
                            //  above in the edit item)? should
                            //  I instead take a more reactive approach with a one-time observer?
                            //  Maybe it would even be
                            //  useful to make a OneTimeObserver class which removes itself.
                            SessionArchiveFragmentViewModel viewModel = getViewModel();
                            int sessionIdToDelete = viewModel.getAllSleepSessionDataIds()
                                    .getValue()
                                    .get(sessionPosition);
                            viewModel.deleteSession(sessionIdToDelete);
                            
                            Snackbar.make(getView(),
                                          "Deleted session #" + sessionPosition,
                                          Snackbar.LENGTH_SHORT).show();
                        }
                    });
            deleteDialog.show(getChildFragmentManager(), SESSION_DELETE_DIALOG);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
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
                            mContextMenuItemPosition = position;
                            v.showContextMenu();
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
    
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionEdit toEditSessionScreen(
            SessionEditData initialEditData)
    {
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionEdit(
                EDIT_SESSION_RESULT, initialEditData);
    }
    
    /**
     * Generates SafeArgs action for navigating to the AddSessionFragment. This is meant to be used
     * in conjunction with NavController.navigate()
     */
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionEdit toAddSessionScreen()
    {
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionEdit(
                ADD_SESSION_RESULT, getViewModel().getDefaultAddSessionData());
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
