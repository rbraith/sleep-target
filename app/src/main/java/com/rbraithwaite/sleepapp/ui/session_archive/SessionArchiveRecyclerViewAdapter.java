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

//*********************************************************
// public helpers
//*********************************************************

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
            this.moodFrame = itemView.findViewById(R.id.session_archive_list_item_mood_frame);
            this.mood = itemView.findViewById(R.id.session_archive_list_item_mood);
            this.tagsFrame = itemView.findViewById(R.id.session_archive_list_item_tags);
            this.tagsLineOne = this.tagsFrame.findViewById(R.id.tags_line_one);
            this.tagsLineTwo = this.tagsFrame.findViewById(R.id.tags_line_two);
            this.tagsMore = this.tagsFrame.findViewById(R.id.tags_more);
            this.ratingIndicator = itemView.findViewById(R.id.session_archive_list_item_rating);
            
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
        Log.d(TAG, "onCreateViewHolder: called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_archive_list_item, parent, false);
        mFragmentProvider.provide().registerForContextMenu(view);
        return new ViewHolder(view, mOnListItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Log.d(TAG, "onBindViewHolder: called, position = " + position);
        // REFACTOR [20-11-14 5:22PM] -- to make more OO, add this as a method in ViewHolder
        bindViewHolderToItem(holder, mItems.get(position));
    }
    
    @Override
    public int getItemCount()
    {
        if (mItems == null) { return 0; }
        
        int itemCount = mItems.size();
        Log.d(TAG, "getItemCount: itemCount is " + itemCount);
        
        return itemCount;
    }
    
//*********************************************************
// api
//*********************************************************

    public void setItems(List<SessionArchiveListItem> items)
    {
        mItems = items;
        notifyDataSetChanged();
    }

//*********************************************************
// private methods
//*********************************************************

    private LifecycleOwner getLifecycleOwner()
    {
        return mFragmentProvider.provide();
    }
    
    private void bindViewHolderToItem(ViewHolder viewHolder, SessionArchiveListItem item)
    {
        viewHolder.data = item;
        
        viewHolder.startTime.setText(item.startTime);
        viewHolder.stopTime.setText(item.endTime);
        viewHolder.duration.setText(item.sessionDuration);
        viewHolder.additionalCommentsIcon.setVisibility(
                item.hasAdditionalComments ?
                        View.VISIBLE : View.GONE);
        
        // TODO [21-06-13 2:46AM] -- It would be nice if it weren't possible for mood
        //  here to be null, so that this was just checking isSet().
        if (item.mood != null &&
            item.mood.isSet()) {
            // REFACTOR [21-06-13 3:02AM] -- This mood frame is a legacy artifact and
            //  needs to be removed.
            viewHolder.moodFrame.setVisibility(View.VISIBLE);
            viewHolder.mood.setMood(item.mood.asIndex());
        } else {
            viewHolder.moodFrame.setVisibility(View.GONE);
        }
        
        if (!item.tags.isEmpty()) {
            viewHolder.tagsFrame.setVisibility(View.VISIBLE);
            setupListItemTagList(
                    viewHolder,
                    item.tags,
                    viewHolder.itemView.getContext());
        } else {
            viewHolder.tagsFrame.setVisibility(View.GONE);
        }
        
        viewHolder.ratingIndicator.setRating(item.rating);
    }
    
    // REFACTOR [21-06-29 4:49PM] -- All these methods relating to the tag display should be
    //  extracted, likely to a custom component class for that tag display.
    // REFACTOR [21-04-21 9:18PM] -- the logic in here is very similar to
    //  TagSelectorController.updateSelectedTagsScrollView - similar lists of tags (though maybe
    //  there are enough key differences, e.g. no scroll view here)
    private void setupListItemTagList(ViewHolder viewHolder, List<String> tags, Context context)
    {
        // TODO [21-04-21 9:00PM] figure out some relation between the frame width and
        //  number of allowed characters per tag line (will depend on the character size - maybe
        //  pull a consistent size from tags_more?) - this will be important for landscape
        //  orientation where the list item has more horizontal space.
//        int frameWidth = viewHolder.tagsFrame.getWidth();
        int maxLineCharacters = 20;
        
        LinearLayout[] lines = {viewHolder.tagsLineOne, viewHolder.tagsLineTwo};
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
            viewHolder.tagsMore.setText(String.format(
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
