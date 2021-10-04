/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.ui.session_archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsResult;
import com.rbraithwaite.sleeptarget.ui.session_details.SessionDetailsFragment;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.ui.utils.RecyclerUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionArchiveFragment
        extends BaseFragment<SessionArchiveFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private RecyclerView mRecyclerView;
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
        handleSessionDetailsResult(getSessionDetailsResult().consumeResult());
        
        initRecyclerView(view);
        initFloatingActionButton(view);
    }
    
    @Override
    protected Properties<SessionArchiveFragmentViewModel> initProperties()
    {
        return new Properties<>(true, SessionArchiveFragmentViewModel.class);
    }

//*********************************************************
// api
//*********************************************************

    public SessionArchiveRecyclerViewAdapter getRecyclerViewAdapter()
    {
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new SessionArchiveRecyclerViewAdapter(
                    getViewModel(),
                    () -> SessionArchiveFragment.this,
                    (v, position) -> navigateToEditSessionScreen(position));
        }
        return mRecyclerViewAdapter;
    }

//*********************************************************
// private methods
//*********************************************************

    private void handleSessionDetailsResult(DetailsResult.Result<SleepSessionWrapper> result)
    {
        if (result == null) {
            // we are not returning from the session details fragment, so do nothing
            return;
        }
        
        switch (result.action) {
        case ADDED:
            getViewModel().addSleepSession(result.data);
            break;
        case UPDATED:
            getViewModel().updateSleepSession(result.data);
            break;
        case DELETED:
            getViewModel().deleteSession(result.data);
            break;
        }
    }
    
    private SessionDetailsFragment.Result getSessionDetailsResult()
    {
        return new ViewModelProvider(requireActivity()).get(SessionDetailsFragment.Result.class);
    }
    
    private void navigateToEditSessionScreen(final int listItemPosition)
    {
        int sessionId = getViewHolderFor(listItemPosition).data.sleepSessionId;
        LiveDataFuture.getValue(
                getViewModel().getSleepSession(sessionId),
                getViewLifecycleOwner(),
                sleepSession -> getNavController().navigate(toEditSessionScreen(
                        sleepSession)));
    }
    
    private SessionArchiveRecyclerViewAdapter.ItemViewHolder getViewHolderFor(int listItemPosition)
    {
        return (SessionArchiveRecyclerViewAdapter.ItemViewHolder) mRecyclerView.findViewHolderForAdapterPosition(
                listItemPosition);
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
                    SessionDetailsFragment.Args args = new SessionDetailsFragment.Args();
                    args.mode = DetailsFragment.Mode.ADD;
                    args.initialData = initialData;
                    
                    SessionArchiveFragmentDirections.ActionSessionArchiveToSessionData
                            toAddSessionScreen =
                            SessionArchiveFragmentDirections.actionSessionArchiveToSessionData(args);
                    
                    getNavController().navigate(toAddSessionScreen);
                });
    }
    
    private SessionArchiveFragmentDirections.ActionSessionArchiveToSessionData toEditSessionScreen(
            SleepSessionWrapper initialEditData)
    {
        SessionDetailsFragment.Args args = new SessionDetailsFragment.Args();
        args.mode = DetailsFragment.Mode.UPDATE;
        args.initialData = initialEditData;
        
        return SessionArchiveFragmentDirections.actionSessionArchiveToSessionData(args);
    }
    
    private void initRecyclerView(@NonNull View fragmentRoot)
    {
        mRecyclerView = fragmentRoot.findViewById(R.id.session_archive_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.addItemDecoration(new RecyclerUtils.VerticalMargin(8, requireContext()));
        mRecyclerView.setAdapter(getRecyclerViewAdapter());
    }
}
