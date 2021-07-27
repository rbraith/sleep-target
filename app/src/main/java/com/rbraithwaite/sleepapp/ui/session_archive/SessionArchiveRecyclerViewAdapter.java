package com.rbraithwaite.sleepapp.ui.session_archive;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.utils.CommonUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.ProviderOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    private MaterialShapeDrawable mTagBackground;

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
        LinearLayout tagsFrame;
        // SMELL [21-04-21 8:49PM] -- These explicitly enumerated sub-views don't feel like a great
        //  solution - take a closer look at this system.
        LinearLayout tagsLineOne;
        LinearLayout tagsLineTwo;
        TextView tagsMore;
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
            this.tagsFrame = itemView.findViewById(R.id.session_archive_list_item_tags);
            this.tagsLineOne = this.tagsFrame.findViewById(R.id.tags_line_one);
            this.tagsLineTwo = this.tagsFrame.findViewById(R.id.tags_line_two);
            this.tagsMore = this.tagsFrame.findViewById(R.id.tags_more);
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
            itemViewHolder.tagsFrame.setVisibility(View.VISIBLE);
            setupListItemTagList(
                    itemViewHolder,
                    item.tags,
                    itemViewHolder.itemView.getContext());
        } else {
            itemViewHolder.tagsFrame.setVisibility(View.GONE);
        }
        
        itemViewHolder.ratingIndicator.setRating(item.rating);
        
        if (item.interruptionsText != null) {
            itemViewHolder.interruptions.setVisibility(View.VISIBLE);
            itemViewHolder.interruptions.setText(item.interruptionsText);
        } else {
            itemViewHolder.interruptions.setVisibility(View.GONE);
        }
    }
    
    // REFACTOR [21-06-29 4:49PM] -- All these methods relating to the tag display should be
    //  extracted, likely to a custom component class for that tag display.
    // REFACTOR [21-04-21 9:18PM] -- the logic in here is very similar to
    //  TagSelectorController.updateSelectedTagsScrollView - similar lists of tags (though maybe
    //  there are enough key differences, e.g. no scroll view here)
    private void setupListItemTagList(
            ItemViewHolder itemViewHolder,
            List<String> tags,
            Context context)
    {
        // TODO [21-04-21 9:00PM] figure out some relation between the frame width and
        //  number of allowed characters per tag line (will depend on the character size - maybe
        //  pull a consistent size from tags_more?) - this will be important for landscape
        //  orientation where the list item has more horizontal space.
//        int frameWidth = viewHolder.tagsFrame.getWidth();
        int maxLineCharacters = 20;
        
        LinearLayout[] lines = {itemViewHolder.tagsLineOne, itemViewHolder.tagsLineTwo};
        for (LinearLayout line : lines) {
            line.removeAllViews();
        }
        
        int currentLineCharacters = 0;
        int lineIndex = 0;
        LinearLayout currentLine = lines[lineIndex];
        int displayedTagsCount = 0;
        for (String tagText : tags) {
            currentLineCharacters += tagText.length();
            
            if (currentLineCharacters >= maxLineCharacters) {
                lineIndex++;
                if (lineIndex == lines.length) {
                    // reached max lines, exit the loop and don't display any more tags
                    break;
                } else {
                    currentLine = lines[lineIndex];
                    currentLineCharacters = tagText.length();
                }
            }
            
            // TODO [21-04-21 9:01PM] I need to account for "long tag text" edge cases
            //      - if a tag exceeds the max allowed characters, cut it off with "..."
            //      - be careful not to drop down to the next line on tags that are too long anyway.
            
            currentLine.addView(generateTagView(tagText, context));
            displayedTagsCount++;
        }
        
        // If there are more tags than could fit in the available lines, display some text
        // indicating these extra tags
        if (displayedTagsCount < tags.size()) {
            itemViewHolder.tagsMore.setText(String.format(
                    Locale.CANADA,
                    context.getString(R.string.session_archive_item_more_tags_text),
                    tags.size() - displayedTagsCount));
        }
    }
    
    private MaterialShapeDrawable getTagBackground(Context context)
    {
        // https://stackoverflow.com/a/61768682
        mTagBackground = CommonUtils.lazyInit(mTagBackground, () -> {
            float cornerRadius = context.getResources()
                    .getDimension(R.dimen.archive_list_item_tag_corner_radius);
            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                    .build();
            return new MaterialShapeDrawable(shapeAppearanceModel);
        });
        return mTagBackground;
    }
    
    // REFACTOR [21-04-21 9:16PM] -- various hardcoded values.
    private View generateTagView(String tagText, Context context)
    {
        // OPTIMIZE [21-04-22 10:55PM] -- These params don't need to be instantiated each time
        //  - use a constant.
        LinearLayout.LayoutParams tagViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tagViewParams.setMargins(0, 0, 15, 0);
        
        MaterialTextView tagView =
                new MaterialTextView(context, null, R.attr.archiveListItemTagStyle);
        tagView.setText(tagText);
        tagView.setBackground(getTagBackground(context));
        
        tagView.setPadding(15, 5, 15, 5);
        tagView.setLayoutParams(tagViewParams);
        
        return tagView;
    }
}
