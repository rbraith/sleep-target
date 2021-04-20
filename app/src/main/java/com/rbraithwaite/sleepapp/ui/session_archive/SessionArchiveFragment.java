package com.rbraithwaite.sleepapp.ui.session_archive;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.dialog.DialogUtils;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragment;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.interfaces.ProviderOf;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends BaseFragment<SessionArchiveFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private SessionArchiveRecyclerViewAdapter mRecyclerViewAdapter;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragment";
    private static final String SESSION_DELETE_DIALOG = "SessionDeleteDialog";
    
    private static final int DATA_ICON_DELETE = R.drawable.ic_baseline_delete_forever_24;

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
                    () -> SessionArchiveFragment.this,
                    this::requireContext,
                    (v, position) -> navigateToEditSessionScreen(position));
        }
        return mRecyclerViewAdapter;
    }

//*********************************************************
// private methods
//*********************************************************

    private void navigateToEditSessionScreen(final int listItemPosition)
    {
        // SMELL [21-12-30 9:35PM] -- These nested anon classes are kinda ugly, try to find a
        //  better way.
        LiveDataFuture.getValue(
                getViewModel().getAllSleepSessionIds(),
                getViewLifecycleOwner(),
                sleepSessionIds -> LiveDataFuture.getValue(
                        getViewModel().getSleepSession(sleepSessionIds.get(listItemPosition)),
                        getViewLifecycleOwner(),
                        initialEditData -> getNavController().navigate(toEditSessionScreen(
                                initialEditData))));
    }
    
    private void initFloatingActionButton(View fragmentRoot)
    {
        FloatingActionButton floatingActionButton =
                fragmentRoot.findViewById(R.id.session_archive_fab);
        floatingActionButton.setOnClickListener(v -> navigateToAddSessionScreen());
    }
    
    private void navigateToAddSessionScreen()
    {
        LiveDataFuture.getValue(
                getViewModel().getInitialAddSessionData(),
                getViewLifecycleOwner(),
                initialData -> {
                    SessionDataFragment.ArgsBuilder argsBuilder =
                            new SessionDataFragment.ArgsBuilder(initialData)
                                    .setPositiveActionListener(new SessionDataFragment.ActionListener()
                                    {
                                        @Override
                                        public void onAction(
                                                SessionDataFragment fragment,
                                                SleepSessionWrapper result)
                                        {
                                            getViewModel().addSleepSession(result);
                                            fragment.completed();
                                        }
                                    });
                    SessionArchiveFragmentDirections.ActionSessionArchiveToSessionData
                            toAddSessionScreen =
                            SessionArchiveFragmentDirections.actionSessionArchiveToSessionData(
                                    argsBuilder.build());
                    
                    getNavController().navigate(toAddSessionScreen);
                }
        );
    }
    
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionData toEditSessionScreen(
            SleepSessionWrapper initialEditData)
    {
        // SMELL [21-12-31 1:50AM] -- IDK if I like this solution, it feels over-engineered,
        //  and I don't like the clients being responsible for handling the fragment completion.
        //  another possible solution:
        //      create derived fragment classes AddSessionFragment & SessionInfoFragment, which
        //      derive from the same base SessionDateFragment
        //      The derived fragments would handle action behaviour & icons, the base would handle
        //      args, results, & up/back button behaviour.
        //  The reason I didn't go with that solution is because I figured safeargs & nav component
        //  would be a massive headache with it.
        SessionDataFragment.ArgsBuilder argsBuilder = new SessionDataFragment.ArgsBuilder(
                initialEditData)
                .setNegativeIcon(DATA_ICON_DELETE)
                .setPositiveActionListener(new SessionDataFragment.ActionListener()
                {
                    @Override
                    public void onAction(SessionDataFragment fragment, SleepSessionWrapper result)
                    {
                        getViewModel().updateSleepSession(result);
                        fragment.completed();
                    }
                })
                .setNegativeActionListener(new SessionDataFragment.ActionListener()
                {
                    @Override
                    public void onAction(
                            final SessionDataFragment fragment,
                            final SleepSessionWrapper result)
                    {
                        AlertDialogFragment deleteDialog = DialogUtils.createDeleteDialog(
                                requireContext(),
                                R.string.session_archive_delete_dialog_title,
                                R.string.session_archive_delete_dialog_message,
                                (dialog, which) -> {
                                    // this is alright since SessionArchiveFragment's
                                    // view model is lifecycle-owned by the activity
                                    int deletedId =
                                            getViewModel().deleteSession(result);
                                    Snackbar.make(
                                            fragment.getView(),
                                            "Deleted session #" + deletedId,
                                            Snackbar.LENGTH_SHORT)
                                            .show();
                                    fragment.completed();
                                });
                        
                        deleteDialog.show(fragment.getChildFragmentManager(),
                                          SESSION_DELETE_DIALOG);
                    }
                });
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionData(
                argsBuilder.build());
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        RecyclerView recyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
