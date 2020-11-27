package com.rbraithwaite.sleepapp.ui.session_archive;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.session_archive.data.UISleepSessionData;

import java.util.List;

public class SessionArchiveRecyclerViewAdapter
        extends RecyclerView.Adapter<SessionArchiveRecyclerViewAdapter.ViewHolder>
{
    // OPTIMIZE [20-11-14 5:20PM] -- consider retaining a cache of retrieved data
    //  points, to speed things up?

//*********************************************************
// private properties
//*********************************************************

    private LifeCycleOwnerProvider mLifeCycleOwnerProvider;
    private SessionArchiveFragmentViewModel mViewModel;
    private LiveData<List<Integer>> mSleepSessionDataIds;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "RecyclerViewAdapter";


//*********************************************************
// public helpers
//*********************************************************

    // inspired by https://stackoverflow.com/a/45336315
    public interface LifeCycleOwnerProvider
    {
        public LifecycleOwner getLifeCycleOwner();
    }

//*********************************************************
// package helpers
//*********************************************************

    static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        TextView startTime;
        TextView stopTime;
        TextView duration;
        
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            
            this.startTime = itemView.findViewById(R.id.session_archive_list_item_start_VALUE);
            this.stopTime = itemView.findViewById(R.id.session_archive_list_item_stop_VALUE);
            this.duration = itemView.findViewById(R.id.session_archive_list_item_duration_VALUE);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveRecyclerViewAdapter(
            SessionArchiveFragmentViewModel viewModel,
            LifeCycleOwnerProvider lifeCycleOwnerProvider)
    {
        mViewModel = viewModel;
        mLifeCycleOwnerProvider = lifeCycleOwnerProvider;
        
        mSleepSessionDataIds = mViewModel.getAllSleepSessionDataIds();
        mSleepSessionDataIds.observe(
                mLifeCycleOwnerProvider.getLifeCycleOwner(),
                new Observer<List<Integer>>()
                {
                    @Override
                    public void onChanged(List<Integer> integers)
                    {
                        notifyDataSetChanged();
                    }
                });
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_archive_list_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // REFACTOR [20-11-14 5:22PM] -- to make more OO, add this as a method in ViewHolder
        //  ViewHolder might need a LifeCycleProvider ctor dependency in this case.
        //  ehhhh maybe don't do this..... since bindToViewModel calls notifyItemChanged
        bindToViewModel(holder, position);
    }
    
    // BUG [20-11-23 3:17AM] -- investigate why this is being called so much (every frame?)
    //  might be some kind of LiveData update problem - eg mViewModel.getAllSleepSessionDataIds()
    //  Try adding a log to the observer in SessionArchiveRecyclerViewAdapter ctor.
    @Override
    public int getItemCount()
    {
        List<Integer> ids = mSleepSessionDataIds.getValue();
        if (ids == null) { return 0; }
        
        int itemCount = ids.size();
        Log.d(TAG, "getItemCount: itemCount is " + itemCount);
        
        return itemCount;
    }

//*********************************************************
// private methods
//*********************************************************

    private void bindToViewModel(final ViewHolder viewHolder, final int position)
    {
        LiveData<UISleepSessionData> uiSleepSessionData = mViewModel.getSleepSessionData(
                mSleepSessionDataIds.getValue().get(position));
        LifecycleOwner lifecycleOwner = mLifeCycleOwnerProvider.getLifeCycleOwner();
        
        // I thought the new Observer here might leak memory and cause duplicate updates, but
        // getSleepSession data provides a new livedata instance so I should be ok?
        uiSleepSessionData.observe(
                lifecycleOwner,
                new Observer<UISleepSessionData>()
                {
                    @Override
                    public void onChanged(UISleepSessionData uiSleepSessionData)
                    {
                        viewHolder.startTime.setText(uiSleepSessionData.startTime);
                        viewHolder.stopTime.setText(uiSleepSessionData.endTime);
                        viewHolder.duration.setText(uiSleepSessionData.sessionDuration);
                        notifyItemChanged(position);
                    }
                });
    }
}
