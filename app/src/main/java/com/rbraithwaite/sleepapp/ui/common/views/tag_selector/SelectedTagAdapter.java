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
// public helpers
//*********************************************************

    public interface OnItemClickListener
    {
        void onItemClick(ViewHolder viewHolder);
    }
    
    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public TagUiData data;
        public TextView tag;
        
        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener)
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
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ViewHolder(createTagView(parent.getContext()), mOnItemClickListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.bindTo(mSelectedTags.get(position));
    }
    
    @Override
    public int getItemCount()
    {
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
