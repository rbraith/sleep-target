package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepInterruptionListItem;

import java.util.List;

// REFACTOR [21-07-19 5:00PM] -- I could extract a pattern for a simple adapter.
public class PostSleepInterruptionsAdapter
        extends RecyclerView.Adapter<PostSleepInterruptionsAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<PostSleepInterruptionListItem> mItems;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        TextView start;
        TextView duration;
        TextView reason;
        
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            start = itemView.findViewById(R.id.postsleep_interruptions_listitem_start);
            duration = itemView.findViewById(R.id.postsleep_interruptions_listitem_duration);
            reason = itemView.findViewById(R.id.postsleep_interruptions_listitem_reason);
        }
        
        public void bindTo(PostSleepInterruptionListItem item)
        {
            start.setText(item.start);
            duration.setText(item.duration);
            reason.setText(item.reason);
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public PostSleepInterruptionsAdapter(List<PostSleepInterruptionListItem> items)
    {
        mItems = items;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ViewHolder(LayoutInflater
                                      .from(parent.getContext())
                                      .inflate(R.layout.postsleep_interruptions_listitem,
                                               parent,
                                               false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.bindTo(mItems.get(position));
    }
    
    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
