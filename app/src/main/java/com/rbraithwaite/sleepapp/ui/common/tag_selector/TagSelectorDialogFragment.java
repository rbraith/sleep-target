package com.rbraithwaite.sleepapp.ui.common.tag_selector;

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

import com.rbraithwaite.sleepapp.R;

public class TagSelectorDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private TagSelectorViewModel mViewModel;
    
    private int mThemeId;
    
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
     *
     * @param viewModel     The viewmodel used in the dialog.
     * @param dialogThemeId The theme that will be applied to the dialog view
     */
    public static TagSelectorDialogFragment createInstance(
            TagSelectorViewModel viewModel,
            int dialogThemeId)
    {
        TagSelectorDialogFragment fragment = new TagSelectorDialogFragment();
        fragment.mViewModel = viewModel;
        fragment.mThemeId = dialogThemeId;
        return fragment;
    }

//*********************************************************
// private methods
//*********************************************************

    private View initTagRecycler()
    {
        RecyclerView tagRecycler = new RecyclerView(new ContextThemeWrapper(
                requireContext(), mThemeId));
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        // These flags fix problems with the keyboard overlapping new tags when they are edited.
        // https://stackoverflow.com/a/40609951
        // Also I like having the 'add new tag' button at the top, and this is an easy way of
        // getting that.
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        tagRecycler.setLayoutManager(layoutManager);
        tagRecycler.setTag(RECYCLER_TAG);
        // On using 'this' instead of getViewLifecycleOwner() in DialogFragments
        //  https://stackoverflow.com/a/60446681
        //  https://developer.android.com/guide/fragments/dialogs#lifecycle
        tagRecycler.setAdapter(new TagSelectorRecyclerAdapter(mViewModel, this));
        return applyKeyboardHackLol(tagRecycler);
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
