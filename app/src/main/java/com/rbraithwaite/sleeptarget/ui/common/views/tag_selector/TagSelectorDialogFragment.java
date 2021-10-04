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
package com.rbraithwaite.sleeptarget.ui.common.views.tag_selector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleeptarget.R;

public class TagSelectorDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private TagSelectorViewModel mViewModel;
    private RecyclerView mTagRecycler;

//*********************************************************
// public constants
//*********************************************************

    public static final String RECYCLER_TAG = "TagSelectorDialog_recycler";

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        mViewModel = TagSelectorViewModel.getInstanceFrom(requireActivity());
        
        // TODO [21-06-14 12:34AM] -- the theme should also be applied to the builder.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(initTagRecycler())
                .setPositiveButton(R.string.close, null);
        
        return builder.create();
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Creates a new instance of this fragment.
     */
    public static TagSelectorDialogFragment createInstance()
    {
        TagSelectorDialogFragment fragment = new TagSelectorDialogFragment();
        return fragment;
    }
    
    public RecyclerView getTagRecycler()
    {
        return mTagRecycler;
    }

//*********************************************************
// private methods
//*********************************************************

    private View initTagRecycler()
    {
        mTagRecycler = new RecyclerView(new ContextThemeWrapper(
                requireContext(), mViewModel.getDialogThemeId()));
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        // These flags fix problems with the keyboard overlapping new tags when they are edited.
        // https://stackoverflow.com/a/40609951
        // Also I like having the 'add new tag' button at the top, and this is an easy way of
        // getting that.
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mTagRecycler.setLayoutManager(layoutManager);
        mTagRecycler.setTag(RECYCLER_TAG);
        // On using 'this' instead of getViewLifecycleOwner() in DialogFragments
        //  https://stackoverflow.com/a/60446681
        //  https://developer.android.com/guide/fragments/dialogs#lifecycle
        mTagRecycler.setAdapter(new TagSelectorRecyclerAdapter(mViewModel, this));
        return applyKeyboardHackLol(mTagRecycler);
    }
    
    // HACK [21-04-9 11:55PM] -- Hacky solution to the problem of the soft keyboard not being
    //  displayed for the EditTexts in the recycler list.
    //  https://stackoverflow.com/a/26400540.
    private View applyKeyboardHackLol(RecyclerView tagRecycler)
    {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        final EditText keyboardHack = new EditText(requireContext());
        keyboardHack.setVisibility(View.GONE);
        
        tagRecycler.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f)); // weight is needed for the view to display
        layout.addView(tagRecycler);
        layout.addView(keyboardHack);
        
        return layout;
    }
}
