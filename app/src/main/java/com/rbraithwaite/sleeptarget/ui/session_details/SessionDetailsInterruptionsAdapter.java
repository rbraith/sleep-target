/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.session_details;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;

import java.util.List;

import static com.rbraithwaite.sleeptarget.ui.utils.RecyclerUtils.inflateLayout;

public class SessionDetailsInterruptionsAdapter
        extends RecyclerView.Adapter<SessionDetailsInterruptionsAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<InterruptionListItem> mItems;
    
    private OnListItemClickListener mOnListItemClickListener;
    private OnAddButtonClickListener mOnAddButtonClickListener;

//*********************************************************
// private constants
//*********************************************************

    private static final int VIEWTYPE_ITEM = 0;
    
    private static final int VIEWTYPE_ADD_BUTTON = 1;
    
//*********************************************************
// public helpers
//*********************************************************

    // REFACTOR [21-07-23 3:12PM] -- this list item clicking system is duplicated in
    //  SessionArchiveRecyclerviewAdapter.
    public interface OnListItemClickListener
    {
        void onClick(ItemViewHolder viewHolder);
    }
    
    public interface OnAddButtonClickListener
    {
        void onClick();
    }
    
    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
    
    // REFACTOR [21-07-21 12:51AM] -- this duplicates PostSleepInterruptionsAdapter.ViewHolder,
    //  is there anything I can do? Maybe create a common base ViewHolder, or just use
    //  RecyclerView.ViewHolder?
    public static class ItemViewHolder
            extends ViewHolder
    {
        InterruptionListItem data;
        
        TextView start;
        TextView duration;
        TextView reason;
        
        public ItemViewHolder(
                @NonNull View itemView,
                OnListItemClickListener onListItemClickListener)
        {
            super(itemView);
            
            start = itemView.findViewById(R.id.common_interruptions_listitem_start);
            duration = itemView.findViewById(R.id.common_interruptions_listitem_duration);
            reason = itemView.findViewById(R.id.common_interruptions_listitem_reason);
            
            itemView.setOnClickListener(v -> {
                if (onListItemClickListener != null) {
                    onListItemClickListener.onClick(this);
                }
            });
        }
        
        public void bindTo(InterruptionListItem item)
        {
            data = item;
            
            start.setText(item.start);
            duration.setText(item.duration);
            reason.setText(item.reason);
        }
    }
    
    public static class AddButtonViewHolder
            extends ViewHolder
    {
        Button button;
        OnAddButtonClickListener onAddButtonClickListener;
        
        public AddButtonViewHolder(@NonNull View itemView, OnAddButtonClickListener onAddButtonClickListener)
        {
            super(itemView);
            button = itemView.findViewById(R.id.session_details_interruptions_addbtn);
            this.onAddButtonClickListener = onAddButtonClickListener;
        }
        
        public void init()
        {
            button.setOnClickListener(v -> {
                if (onAddButtonClickListener != null) {
                    onAddButtonClickListener.onClick();
                }
            });
        }
    }


//*********************************************************
// constructors
//*********************************************************

    public SessionDetailsInterruptionsAdapter(
            List<InterruptionListItem> items,
            OnListItemClickListener onListItemClickListener)
    {
        mItems = items;
        mOnListItemClickListener = onListItemClickListener;
    }

//*********************************************************
// overrides
//*********************************************************
    
    
    public void setOnAddButtonClickListener(OnAddButtonClickListener onAddButtonClickListener)
    {
        mOnAddButtonClickListener = onAddButtonClickListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (viewType) {
        case VIEWTYPE_ITEM:
            View item = inflateLayout(R.layout.session_details_interruptions_listitem, parent);
            return new ItemViewHolder(item, mOnListItemClickListener);
        
        case VIEWTYPE_ADD_BUTTON:
            View addButton = inflateLayout(R.layout.session_details_interruptions_addbtn, parent);
            return new AddButtonViewHolder(addButton, mOnAddButtonClickListener);
        
        default:
            throw new IllegalArgumentException("Invalid viewType: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        switch (getItemViewType(position)) {
        case VIEWTYPE_ITEM:
            ((ItemViewHolder) holder).bindTo(mItems.get(position - 1));
            break;
        case VIEWTYPE_ADD_BUTTON:
            ((AddButtonViewHolder) holder).init();
            break;
        }
    }
    
    @Override
    public int getItemViewType(int position)
    {
        return position == 0 ? VIEWTYPE_ADD_BUTTON : VIEWTYPE_ITEM;
    }
    
    @Override
    public int getItemCount()
    {
        return mItems.size() + 1; // +1 for add button
    }
}