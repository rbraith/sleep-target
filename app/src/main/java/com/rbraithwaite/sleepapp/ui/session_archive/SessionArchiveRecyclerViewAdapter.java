package com.rbraithwaite.sleepapp.ui.session_archive;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

    private FragmentProvider mFragmentProvider;
    private SessionArchiveFragmentViewModel mViewModel;
    private LiveData<List<Integer>> mSleepSessionDataIds;
    
    private OnListItemClickListener mOnListItemClickListener;


//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "RecyclerViewAdapter";

//*********************************************************
// public helpers
//*********************************************************

    public interface FragmentProvider
    {
        public Fragment getFragment();
    }
    
    public interface OnListItemClickListener
    {
        public void onClick(View v, int position);
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
        
        public ViewHolder(
                @NonNull View itemView,
                final SessionArchiveRecyclerViewAdapter.OnListItemClickListener onListItemClickListener)
        {
            super(itemView);
            Log.d(TAG, "new viewholder created");
            
            // REFACTOR [20-12-11 3:08PM] -- rename stop to end.
            this.startTime = itemView.findViewById(R.id.session_archive_list_item_start_VALUE);
            this.stopTime = itemView.findViewById(R.id.session_archive_list_item_stop_VALUE);
            this.duration = itemView.findViewById(R.id.session_archive_list_item_duration_VALUE);
            
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (onListItemClickListener != null) {
                        onListItemClickListener.onClick(v, getAdapterPosition());
                    }
//                    v.showContextMenu();
                }
            });
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveRecyclerViewAdapter(
            SessionArchiveFragmentViewModel viewModel,
            FragmentProvider fragmentProvider,
            OnListItemClickListener onListItemClickListener)
    {
        Log.d(TAG, "ctor called");
        mViewModel = viewModel;
        mFragmentProvider = fragmentProvider;
        mOnListItemClickListener = onListItemClickListener;
        
        mSleepSessionDataIds = mViewModel.getAllSleepSessionDataIds();
        mSleepSessionDataIds.observe(
                getLifecycleOwner(),
                new Observer<List<Integer>>()
                {
                    @Override
                    public void onChanged(List<Integer> integers)
                    {
                        Log.d(TAG, "onChanged: session data id list changed, notifying...");
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
        Log.d(TAG, "onCreateViewHolder: called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_archive_list_item, parent, false);
        mFragmentProvider.getFragment().registerForContextMenu(view);
        return new ViewHolder(view, mOnListItemClickListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Log.d(TAG, "onBindViewHolder: called, position = " + position);
        // REFACTOR [20-11-14 5:22PM] -- to make more OO, add this as a method in ViewHolder
        //  ViewHolder might need a LifeCycleProvider ctor dependency in this case.
        //  ehhhh maybe don't do this..... since bindToViewModel calls notifyItemChanged
        bindToViewModel(holder, position);
    }
    
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

    private LifecycleOwner getLifecycleOwner()
    {
        return mFragmentProvider.getFragment();
    }
    
    private void bindToViewModel(final ViewHolder viewHolder, final int position)
    {
        LiveData<UISleepSessionData> uiSleepSessionData = mViewModel.getSleepSessionData(
                mSleepSessionDataIds.getValue().get(position));
        
        // I thought the new Observer here might leak memory and cause duplicate updates, but
        // getSleepSession data provides a new livedata instance so I should be ok?
        uiSleepSessionData.observe(
                getLifecycleOwner(),
                new Observer<UISleepSessionData>()
                {
                    @Override
                    public void onChanged(UISleepSessionData uiSleepSessionData)
                    {
                        Log.d(TAG, "onChanged: item data changed! updating...");
                        if (uiSleepSessionData != null) {
                            viewHolder.startTime.setText(uiSleepSessionData.startTime);
                            viewHolder.stopTime.setText(uiSleepSessionData.endTime);
                            viewHolder.duration.setText(uiSleepSessionData.sessionDuration);
                        }
                    }
                });
    }
}
