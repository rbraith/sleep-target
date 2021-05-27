package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.UiUtils;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import java.util.List;

public class TagSelectorRecyclerAdapter
        extends RecyclerView.Adapter<TagSelectorRecyclerAdapter.ViewHolder>
{
//*********************************************************
// private properties
//*********************************************************

    private List<TagSelectorViewModel.ListItemData> mListItems;
    
    private TagSelectorViewModel mViewModel;
    private boolean mInitialized = false;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "TagSelectorRecyclerAdap";
    private static final int VIEW_TYPE_TAG = 0;
    private static final int VIEW_TYPE_ADD_CUSTOM_BUTTON = 1;

//*********************************************************
// public helpers
//*********************************************************
    
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
    
    public static class AddCustomButtonViewHolder extends ViewHolder
    {
        private static final String TAG = "AddCustomButtonViewHold";
        
        private Button mAddTagButton;
        private EditText mAddTagEditText;
        
        private TagSelectorViewModel mViewModel;
        
        public AddCustomButtonViewHolder(@NonNull View itemView, TagSelectorViewModel viewModel)
        {
            super(itemView);
            
            mViewModel = viewModel;
            mAddTagButton = itemView.findViewById(R.id.tag_add_btn);
            mAddTagEditText = itemView.findViewById(R.id.tag_add_btn_edittext);
        }
        
        public void rebind()
        {
            mAddTagButton.setVisibility(View.VISIBLE);
            mAddTagEditText.setVisibility(View.INVISIBLE);
            
            mAddTagButton.setOnClickListener(v -> {
                mAddTagButton.setVisibility(View.GONE);
                mAddTagEditText.setVisibility(View.VISIBLE);
                
                mAddTagEditText.requestFocus();
            });
            
            mAddTagEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    UiUtils.closeSoftKeyboard(mAddTagEditText);
                    mAddTagButton.setVisibility(View.VISIBLE);
                    mAddTagEditText.setVisibility(View.INVISIBLE);
                    
                    mViewModel.addTagFromText(mAddTagEditText.getText().toString());
                    mAddTagEditText.setText("");
                }
                return false;
            });
        }
    }
    
    public static class TagViewHolder extends ViewHolder
    {
        private static final String TAG = "TagViewHolder";
        
        private CardView mCard;
        private TextView mTagText;
        private EditText mTagEditText;
        private Button mTagEditButton;
        private Button mTagDeleteButton;
        
        private TagSelectorViewModel mViewModel;
        
        private Attributes mAttributes;
        
        public TagViewHolder(@NonNull View itemView, TagSelectorViewModel viewModel, Attributes attributes)
        {
            super(itemView);
            
            mViewModel = viewModel;
            
            mTagText = itemView.findViewById(R.id.tag_text);
            mTagEditText = itemView.findViewById(R.id.tag_edittext);
            mTagEditButton = itemView.findViewById(R.id.tag_edit_btn);
            mTagDeleteButton = itemView.findViewById(R.id.tag_delete_btn);
            mCard = itemView.findViewById(R.id.tag_selector_dialog_list_item_card);
            
            mAttributes = attributes;
        }
        
        private void expandView()
        {
            mTagEditButton.setVisibility(View.VISIBLE);
            mTagDeleteButton.setVisibility(View.VISIBLE);
        }
        
        private void collapseView()
        {
            mTagEditButton.setVisibility(View.GONE);
            mTagDeleteButton.setVisibility(View.GONE);
        }
        
        private void setEditTextActive(boolean isActive)
        {
            if (isActive) {
                mTagText.setVisibility(View.INVISIBLE);
                mTagEditText.setVisibility(View.VISIBLE);
                
                mTagEditText.requestFocus();
            } else {
                mTagText.setVisibility(View.VISIBLE);
                mTagEditText.setVisibility(View.INVISIBLE);
            }
        }
        
        public void bindTo(final TagSelectorViewModel.ListItemData listItemData)
        {
            if (listItemData.expanded) {
                expandView();
                
                mTagEditButton.setOnClickListener(v -> {
                    mViewModel.toggleTagEditState(getAdapterPosition());
                });
                mTagDeleteButton.setOnClickListener(v -> mViewModel.deleteTag(getAdapterPosition()));
            } else {
                collapseView();
            }
            setEditTextActive(listItemData.beingEdited);
            mCard.setCardBackgroundColor(listItemData.selected ?
                    mAttributes.selectedTagColor : mAttributes.unselectedTagColor);
            
            mTagText.setText(listItemData.tagUiData.text);
            mTagEditText.setText(listItemData.tagUiData.text);
            mTagEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    UiUtils.closeSoftKeyboard(mTagEditText);
                    
                    mViewModel.updateTagText(
                            getAdapterPosition(),
                            mTagEditText.getText().toString());
                    
                    mViewModel.toggleTagEditState(getAdapterPosition());
                }
                return false;
            });
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public TagSelectorRecyclerAdapter(TagSelectorViewModel viewModel, LifecycleOwner lifecycleOwner)
    {
        mViewModel = viewModel;
        bindViewModel(lifecycleOwner);
    }
    
    private static class Attributes
    {
        public final int unselectedTagColor;
        public final int selectedTagColor;
    
        public Attributes(int unselectedTagColor, int selectedTagColor)
        {
            this.unselectedTagColor = unselectedTagColor;
            this.selectedTagColor = selectedTagColor;
        }
    }
    
    private Attributes mAttributes;
    
    private void maybeInitAttributes(ViewGroup parent)
    {
        mAttributes = CommonUtils.lazyInit(mAttributes, () -> {
            TypedArray ta = parent.getContext().obtainStyledAttributes(R.styleable.TagSelectorDialog);
            Attributes attributes = new Attributes(
                    ta.getColor(R.styleable.TagSelectorDialog_tagSelectorListItemUnselectedColor, -1),
                    ta.getColor(R.styleable.TagSelectorDialog_tagSelectorListItemSelectedColor, -1));
            ta.recycle();
            return attributes;
        });
    }
    
    private Attributes getAttributes()
    {
        return mAttributes;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        maybeInitAttributes(parent);
        
        switch (viewType) {
        case VIEW_TYPE_TAG:
            View tagView = inflateLayout(R.layout.tag_selector_dialog_list_item, parent);
            return new TagViewHolder(tagView, mViewModel, getAttributes());
        
        case VIEW_TYPE_ADD_CUSTOM_BUTTON:
            View addButton = inflateLayout(R.layout.tag_selector_dialog_add_btn, parent);
            return new AddCustomButtonViewHolder(addButton, mViewModel);
        
        default:
            // REFACTOR [21-04-11 11:20PM] -- raise an exception here instead.
            return null;
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position)
    {
        switch (getItemViewType(position)) {
        case VIEW_TYPE_TAG:
            bindTagViewHolder((TagViewHolder) holder, position);
            break;
        case VIEW_TYPE_ADD_CUSTOM_BUTTON:
            bindAddCustomButtonViewHolder((AddCustomButtonViewHolder) holder);
            break;
        }
    }
    
    @Override
    public int getItemViewType(int position)
    {
        if (!(mListItems == null || mListItems.isEmpty()) &&
            position < mListItems.size()) {
            return VIEW_TYPE_TAG;
        }
        return VIEW_TYPE_ADD_CUSTOM_BUTTON;
    }
    
    @Override
    public int getItemCount()
    {
        // +1 since the last item is always the "add custom tag" button
        if (mListItems == null) {
            return 1;
        }
        return mListItems.size() + 1;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void bindViewModel(LifecycleOwner lifecycleOwner)
    {
        mViewModel.getLastListItemChange().observe(
                lifecycleOwner,
                listTrackingData -> {
                    mListItems = listTrackingData.list;
                    
                    if (!mInitialized) {
                        mInitialized = true;
                        notifyDataSetChanged();
                        return;
                    }
                    
                    if (listTrackingData.lastChange == null) {
                        notifyDataSetChanged();
                        return;
                    }
                    
                    switch (listTrackingData.lastChange.changeType) {
                    case ADDED:
                        notifyItemInserted(listTrackingData.lastChange.index);
                        break;
                    case DELETED:
                        notifyItemRemoved(listTrackingData.lastChange.index);
                        break;
                    // MODIFIED is currently unused, as the only time the list item is modified
                    // is when it is edited, and the ViewHolder handles that display update itself.
                    }
                });
        
        mViewModel.getTagExpansionChangedIndices().observe(
                lifecycleOwner,
                changedTags -> {
                    for (Integer tagIndex : changedTags) {
                        notifyItemChanged(tagIndex);
                    }
                });
        
        mViewModel.getTagEditChangeIndices().observe(
                lifecycleOwner,
                changedTags -> {
                    for (Integer tagIndex : changedTags) {
                        notifyItemChanged(tagIndex);
                    }
                });
        
        mViewModel.getLastTagSelectionChangeIndex().observe(
                lifecycleOwner,
                this::notifyItemChanged);
    }
    
    // REFACTOR [21-04-17 2:52PM] -- I could extract this as a general utility for recycler
    //  adapters.
    private View inflateLayout(int layoutId, ViewGroup parent)
    {
        return LayoutInflater
                .from(parent.getContext())
                .inflate(layoutId, parent, false);
    }
    
    private void bindAddCustomButtonViewHolder(AddCustomButtonViewHolder holder)
    {
        holder.rebind();
    }
    
    private void bindTagViewHolder(TagViewHolder holder, final int position)
    {
        final TagSelectorViewModel.ListItemData listItemData = mListItems.get(position);
        
        holder.bindTo(listItemData);
        
        // short click to select an item
        holder.itemView.setOnClickListener(v -> mViewModel.toggleTagSelection(position));
        
        // long click to expand additional options for that item
        holder.itemView.setOnLongClickListener(v -> {
            mViewModel.toggleTagExpansion(position);
            return true;
        });
    }
}
