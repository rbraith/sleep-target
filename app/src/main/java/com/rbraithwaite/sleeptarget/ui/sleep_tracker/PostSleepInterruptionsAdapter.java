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

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;

import java.util.List;

// REFACTOR [21-07-19 5:00PM] -- I could extract a pattern for a simple adapter.
public class PostSleepInterruptionsAdapter
        extends RecyclerView.Adapter<PostSleepInterruptionsAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<InterruptionListItem> mItems;

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
            start = itemView.findViewById(R.id.common_interruptions_listitem_start);
            duration = itemView.findViewById(R.id.common_interruptions_listitem_duration);
            reason = itemView.findViewById(R.id.common_interruptions_listitem_reason);
        }
        
        public void bindTo(InterruptionListItem item)
        {
            start.setText(item.start);
            duration.setText(item.duration);
            reason.setText(item.reason);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public PostSleepInterruptionsAdapter(List<InterruptionListItem> items)
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
                                      .inflate(R.layout.common_interruptions_listitem,
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
