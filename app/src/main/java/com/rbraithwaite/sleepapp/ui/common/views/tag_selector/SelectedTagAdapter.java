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

package com.rbraithwaite.sleepapp.ui.common.views.tag_selector;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectedTagAdapter
        extends RecyclerView.Adapter<SelectedTagAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<TagUiData> mSelectedTags = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    
//*********************************************************
// private constants
//*********************************************************

    private static final int VIEWTYPE_TAG = 0;
    private static final int VIEWTYPE_NO_TAGS = 1;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnItemClickListener
    {
        void onItemClick(ViewHolder viewHolder);
    }
    
    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }

    public static class TagViewHolder
            extends ViewHolder
    {
        public TagUiData data;
        public TextView tag;
        
        public TagViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener)
        {
            super(itemView);
            tag = (TextView) itemView;
            
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(this);
                }
            });
        }
        
        public void bindTo(TagUiData data)
        {
            tag.setText(data.text);
            this.data = data;
        }
    }
    
    public static class NoTagsViewHolder
            extends ViewHolder
    {
        public NoTagsViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
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
            return new TagViewHolder(createTagView(parent.getContext()), mOnItemClickListener);
        case VIEWTYPE_NO_TAGS:
            return new NoTagsViewHolder(createNoTagsMessage(parent.getContext()));
        default:
            throw new IllegalArgumentException("Invalid viewType: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if (getItemViewType(position) == VIEWTYPE_TAG) {
            ((TagViewHolder) holder).bindTo(mSelectedTags.get(position));
        }
    }
    
    @Override
    public int getItemViewType(int position)
    {
        if (mSelectedTags == null || mSelectedTags.isEmpty()) {
            return VIEWTYPE_NO_TAGS;
        }
        return VIEWTYPE_TAG;
    }
    
    @Override
    public int getItemCount()
    {
        if (mSelectedTags == null || mSelectedTags.isEmpty()) {
            return 1;
        }
        return mSelectedTags.size();
    }
    
//*********************************************************
// api
//*********************************************************

    public void setSelectedTags(List<TagUiData> selectedTags)
    {
        mSelectedTags = selectedTags;
        notifyDataSetChanged();
    }
    
    public void setOnItemClickedListener(OnItemClickListener onItemClickedListener)
    {
        mOnItemClickListener = onItemClickedListener;
    }

//*********************************************************
// private methods
//*********************************************************

    private View createTagView(Context context)
    {
        MaterialButton tagView = new MaterialButton(context, null, R.attr.tagSelectorTagStyle);
        tagView.setClickable(false);
        UiUtils.setViewPadding(tagView, 6, 6, 2, 2);
        tagView.setLayoutParams(createTagViewLayoutParams(context));
        return tagView;
    }
    
    private View createNoTagsMessage(Context context)
    {
        // REFACTOR [21-08-17 11:18PM] -- fix this resource reference.
        TextView noTagsMessage =
                new TextView(context, null, R.attr.trackerPostDialogNullDataMessageStyle);
        noTagsMessage.setText(R.string.no_tags);
        return noTagsMessage;
    }
    
    private FlexboxLayoutManager.LayoutParams createTagViewLayoutParams(Context context)
    {
        FlexboxLayoutManager.LayoutParams newParams = new FlexboxLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int dp4 = UiUtils.convertDpToPx(4, context);
        newParams.setMargins(dp4, dp4, 0, 0);
        
        return newParams;
    }
}
