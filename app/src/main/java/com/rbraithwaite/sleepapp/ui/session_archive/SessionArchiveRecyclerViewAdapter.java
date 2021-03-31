package com.rbraithwaite.sleepapp.ui.session_archive;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;

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
        ImageView additionalCommentsIcon;
        
        public ViewHolder(
                @NonNull View itemView,
                final SessionArchiveRecyclerViewAdapter.OnListItemClickListener onListItemClickListener)
        {
            super(itemView);
            Log.d(TAG, "new viewholder created");
            
            this.startTime = itemView.findViewById(R.id.session_archive_list_item_start_VALUE);
            // REFACTOR [20-12-11 3:08PM] -- rename stop to end.
            this.stopTime = itemView.findViewById(R.id.session_archive_list_item_stop_VALUE);
            this.duration = itemView.findViewById(R.id.session_archive_list_item_duration_VALUE);
            this.additionalCommentsIcon =
                    itemView.findViewById(R.id.session_archive_list_item_comment_icon);
            
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (onListItemClickListener != null) {
                        onListItemClickListener.onClick(v, getAdapterPosition());
                    }
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
        
        // SMELL [21-03-24 10:39PM] consider a different solution for displayed the sessions
        //  - do some research on conventional patterns.
        mSleepSessionDataIds = mViewModel.getAllSleepSessionIds();
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
        // REFACTOR [21-01-13 9:17PM] -- use a LiveDataFuture here to get rid of this getValue call?
        LiveData<SessionArchiveListItem> listItem = mViewModel.getListItemData(
                mSleepSessionDataIds.getValue().get(position));
        
        // I thought the new Observer here might leak memory and cause duplicate updates, but
        // getSleepSession data provides a new livedata instance so I should be ok?
        listItem.observe(
                getLifecycleOwner(),
                new Observer<SessionArchiveListItem>()
                {
                    @Override
                    public void onChanged(SessionArchiveListItem sessionArchiveListItem)
                    {
                        Log.d(TAG, "onChanged: item data changed! updating...");
                        if (sessionArchiveListItem != null) {
                            viewHolder.startTime.setText(sessionArchiveListItem.startTime);
                            viewHolder.stopTime.setText(sessionArchiveListItem.endTime);
                            viewHolder.duration.setText(sessionArchiveListItem.sessionDuration);
                            viewHolder.additionalCommentsIcon.setVisibility(
                                    sessionArchiveListItem.hasAdditionalComments ?
                                            View.VISIBLE : View.GONE);
                        }
                    }
                });
    }
}
