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

package com.rbraithwaite.sleeptarget.ui.session_archive;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleeptarget.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleeptarget.utils.interfaces.ProviderOf;

import java.util.ArrayList;
import java.util.List;

// BUG [21-04-22 2:08AM] -- I think there's a bug in here somewhere related to LiveData updates
//  causing the recycler to refresh too many times.
public class SessionArchiveRecyclerViewAdapter
        extends RecyclerView.Adapter<SessionArchiveRecyclerViewAdapter.ViewHolder>
{
    // OPTIMIZE [20-11-14 5:20PM] -- consider retaining a cache of retrieved data
    //  points, to speed things up?

//*********************************************************
// private properties
//*********************************************************

    private ProviderOf<Fragment> mFragmentProvider;
    
    private List<SessionArchiveListItem> mItems = new ArrayList<>();
    
    private OnListItemClickListener mOnListItemClickListener;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "RecyclerViewAdapter";
    
    private static final int VIEW_TYPE_ITEM = 0;
    
    private static final int VIEW_TYPE_NO_DATA = 1;

//*********************************************************
// public helpers
//*********************************************************

    // REFACTOR [21-07-23 3:33PM] change this to be more like SessionDetailsInterruptionsAdapter
    //  - return viewmodel, & viewmodel contains the session id (it contains list item data which
    //  contains the id).
    public interface OnListItemClickListener
    {
        void onClick(View v, int position);
    }

//*********************************************************
// package helpers
//*********************************************************

    static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
    
    static class NoDataViewHolder
            extends ViewHolder
    {
        public NoDataViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
    
    static class ItemViewHolder
            extends ViewHolder
    {
        // REFACTOR [21-06-29 10:19PM] -- I should consider maybe just keeping the sleep session id?
        //  since that's all I'm using this for currently.
        SessionArchiveListItem data;
        
        TextView startTime;
        TextView stopTime;
        TextView duration;
        ImageView additionalCommentsIcon;
        FrameLayout moodFrame;
        MoodView mood;
        RecyclerView tagsRecycler;
        RatingBar ratingIndicator;
        TextView interruptions;
        
        public ItemViewHolder(
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
            this.moodFrame = itemView.findViewById(R.id.session_archive_list_item_mood_frame);
            this.mood = itemView.findViewById(R.id.session_archive_list_item_mood);
            this.tagsRecycler = itemView.findViewById(R.id.session_archive_list_item_tags);
            this.ratingIndicator = itemView.findViewById(R.id.session_archive_list_item_rating);
            this.interruptions =
                    itemView.findViewById(R.id.session_archive_list_item_interruptions_VALUE);
            
            itemView.setOnClickListener(v -> {
                if (onListItemClickListener != null) {
                    onListItemClickListener.onClick(v, getAdapterPosition());
                }
            });
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveRecyclerViewAdapter(
            SessionArchiveFragmentViewModel viewModel,
            ProviderOf<Fragment> fragmentProvider,
            OnListItemClickListener onListItemClickListener)
    {
        Log.d(TAG, "ctor called");
        mFragmentProvider = fragmentProvider;
        mOnListItemClickListener = onListItemClickListener;
        
        viewModel.getAllListItems().observe(getLifecycleOwner(), this::setItems);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (viewType) {
        case VIEW_TYPE_NO_DATA:
            View noDataView = inflateLayout(R.layout.session_archive_no_data, parent);
            return new NoDataViewHolder(noDataView);
        case VIEW_TYPE_ITEM:
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_archive_list_item, parent, false);
            mFragmentProvider.provide().registerForContextMenu(itemView);
            return new ItemViewHolder(itemView, mOnListItemClickListener);
        default:
            throw new IllegalArgumentException("Invalid viewType: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        switch (getItemViewType(position)) {
        case VIEW_TYPE_NO_DATA:
            return;
        case VIEW_TYPE_ITEM:
            // REFACTOR [20-11-14 5:22PM] -- to make more OO, add this as a method in ItemViewHolder
            bindViewHolderToItem((ItemViewHolder) holder, mItems.get(position));
        }
    }
    
    @Override
    public int getItemViewType(int position)
    {
        if (hasNoData()) {
            if (position != 0) {
                // do I throw here? this shouldn't be possible
            }
            return VIEW_TYPE_NO_DATA;
        }
        return VIEW_TYPE_ITEM;
    }
    
    @Override
    public int getItemCount()
    {
        if (hasNoData()) {
            return 1;
        }
        return mItems.size();
    }

//*********************************************************
// api
//*********************************************************

    public boolean hasNoData()
    {
        return mItems == null || mItems.isEmpty();
    }
    
    public void setItems(List<SessionArchiveListItem> items)
    {
        mItems = items;
        notifyDataSetChanged();
    }

//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-07-1 12:59AM] -- copied from TagSelectorRecyclerAdapter.
    private View inflateLayout(int layoutId, ViewGroup parent)
    {
        return LayoutInflater
                .from(parent.getContext())
                .inflate(layoutId, parent, false);
    }
    
    private LifecycleOwner getLifecycleOwner()
    {
        return mFragmentProvider.provide();
    }
    
    private void bindViewHolderToItem(ItemViewHolder itemViewHolder, SessionArchiveListItem item)
    {
        itemViewHolder.data = item;
        
        itemViewHolder.startTime.setText(item.startTime);
        itemViewHolder.stopTime.setText(item.endTime);
        itemViewHolder.duration.setText(item.sessionDuration);
        itemViewHolder.additionalCommentsIcon.setVisibility(
                item.hasAdditionalComments ?
                        View.VISIBLE : View.GONE);
        
        // TODO [21-06-13 2:46AM] -- It would be nice if it weren't possible for mood
        //  here to be null, so that this was just checking isSet().
        if (item.mood != null &&
            item.mood.isSet()) {
            // REFACTOR [21-06-13 3:02AM] -- This mood frame is a legacy artifact and
            //  needs to be removed.
            itemViewHolder.moodFrame.setVisibility(View.VISIBLE);
            itemViewHolder.mood.setMood(item.mood.asIndex());
        } else {
            itemViewHolder.moodFrame.setVisibility(View.GONE);
        }
        
        if (!item.tags.isEmpty()) {
            RecyclerView tagsRecycler = itemViewHolder.tagsRecycler;
            Context context = itemViewHolder.itemView.getContext();
            tagsRecycler.setVisibility(View.VISIBLE);
            tagsRecycler.post(() -> {
                // HACK [21-08-16 8:15PM] -- post is needed because the adapter needs info about
                //  the owner's dimensions
                tagsRecycler.setAdapter(new SessionArchiveListItemTagsAdapter(tagsRecycler,
                                                                              item.tags));
                tagsRecycler.setLayoutManager(new FlexboxLayoutManager(context));
            });
        } else {
            itemViewHolder.tagsRecycler.setVisibility(View.GONE);
        }
        
        itemViewHolder.ratingIndicator.setRating(item.rating);
        
        if (item.interruptionsText != null) {
            itemViewHolder.interruptions.setVisibility(View.VISIBLE);
            itemViewHolder.interruptions.setText(item.interruptionsText);
        } else {
            itemViewHolder.interruptions.setVisibility(View.GONE);
        }
    }
}
