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
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;

import java.util.List;
import java.util.Locale;

public class SessionArchiveListItemTagsAdapter
        extends RecyclerView.Adapter<SessionArchiveListItemTagsAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<String> mTags;
    private int mTagsToDisplayCount;
    private RecyclerView mOwner;
    private MaterialShapeDrawable mTagBackground;

//*********************************************************
// private constants
//*********************************************************

    private static final int VIEWTYPE_TAG = 0;
    private static final int VIEWTYPE_MORE = 1;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveListItemTagsAdapter(RecyclerView owner, List<String> tags)
    {
        mOwner = owner;
        mTags = tags;
        mTagsToDisplayCount = computeTagsToDisplayCount(mTags, mOwner);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (viewType) {
        case VIEWTYPE_TAG:
            return new ViewHolder(createTagView(parent.getContext()));
        case VIEWTYPE_MORE:
            return new ViewHolder(createMoreTagsView(parent.getContext()));
        default:
            throw new IllegalArgumentException("Unknown viewtype: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if (getItemViewType(position) == VIEWTYPE_TAG) {
            ((TextView) holder.itemView).setText(mTags.get(position));
        }
    }
    
    @Override
    public int getItemViewType(int position)
    {
        return position == mTagsToDisplayCount ? VIEWTYPE_MORE : VIEWTYPE_TAG;
    }
    
    @Override
    public int getItemCount()
    {
        if (mTags.isEmpty() || mTags.size() == mTagsToDisplayCount) {
            return mTags.size();
        }
        
        // + 1 for the 'more' view
        return mTagsToDisplayCount + 1;
    }
    
//*********************************************************
// api
//*********************************************************

    public List<String> getTags()
    {
        return mTags;
    }
    
//*********************************************************
// private methods
//*********************************************************

    // HACK [21-08-16 7:42PM] -- This is trying to approximately predict the behaviour of
    //  FlexBoxLayoutManager - it would be much better to just know.
    private int computeTagsToDisplayCount(List<String> tags, RecyclerView owner)
    {
        if (tags == null || tags.isEmpty()) { return 0; }
        
        int lineWidth = owner.getMeasuredWidth();
        // approx because I think the chars are different widths, but even if its approximate its
        // good enough.
        // REFACTOR [21-08-16 8:26PM] -- hardcoded sp val - this matches the value in
        //  @style/ArchiveListItemTagStyle.
        int approxCharWidth = UiUtils.convertSpToPx(10, owner.getContext());
        
        int tagsToDisplayCount = 0;
        
        int lineCount = 0;
        int currentWidth = 0;
        for (String tag : tags) {
            int tagWidth = tag.length() * approxCharWidth;
            boolean isFirstTag = currentWidth == 0 && lineCount == 0;
            
            // dealing with big tags
            if (isFirstTag) {
                // if the first tag takes up 2 lines, display only that tag
                if (lineWidth < tagWidth && tagWidth < (lineWidth * 2)) {
                    ++tagsToDisplayCount;
                    break;
                }
                // don't display the tag if it takes up more than 2 lines
                if (tagWidth > (lineWidth * 2)) {
                    break;
                }
            } else if (tagWidth > lineWidth) {
                // don't allow big tags after the first tag
                break;
            }
            
            currentWidth += tagWidth;
            if (currentWidth >= lineWidth) {
                ++lineCount;
                currentWidth = tagWidth;
            }
            if (lineCount > 2) {
                break;
            }
            
            // only add the tag if we haven't broken due to the line count
            ++tagsToDisplayCount;
        }
        return tagsToDisplayCount;
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
    private View createTagView(Context context)
    {
        int dp6 = UiUtils.convertDpToPx(6, context);
        int dp2 = UiUtils.convertDpToPx(2, context);
        
        // REFACTOR [21-08-16 8:59PM] -- similar logic to SelectedTagAdapter.createTagView()
        FlexboxLayoutManager.LayoutParams tagViewParams = new FlexboxLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tagViewParams.setMargins(0, 0, dp6, dp6);
        
        MaterialTextView tagView =
                new MaterialTextView(context, null, R.attr.archiveListItemTagStyle);
        tagView.setBackground(getTagBackground(context));
        tagView.setPadding(dp6, dp2, dp6, dp2);
        tagView.setLayoutParams(tagViewParams);
        
        return tagView;
    }
    
    private View createMoreTagsView(Context context)
    {
        TextView moreText = new TextView(new ContextThemeWrapper(context,
                                                                 R.style.ArchiveListItemMoreTagsStyle));
        
        moreText.setText(String.format(
                Locale.CANADA,
                context.getString(
                        R.string.session_archive_item_more_tags_text),
                mTags.size() - mTagsToDisplayCount));
        
        return moreText;
    }
}
