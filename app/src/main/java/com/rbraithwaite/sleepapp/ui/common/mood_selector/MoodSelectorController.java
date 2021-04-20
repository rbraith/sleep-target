package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;

public class MoodSelectorController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    
    private Button mAddButton;
    private FrameLayout mMoodValue;
    
    private MoodSelectorViewModel mViewModel;
    private MoodViewFactory mMoodViewFactory;
    
    private LifecycleOwner mLifecycleOwner;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private Callbacks mCallbacks;
    private int mLastSelectedIndex;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_TAG = "MoodSelectorController_dialog";

//*********************************************************
// public helpers
//*********************************************************

    public interface Callbacks
    {
        void onMoodChanged(MoodUiData newMood);
        void onMoodDeleted();
    }

//*********************************************************
// constructors
//*********************************************************

    public MoodSelectorController(
            View root,
            MoodSelectorViewModel viewModel,
            Context context,
            LifecycleOwner lifecycleOwner,
            final FragmentManager fragmentManager)
    {
        mRoot = root;
        
        mAddButton = root.findViewById(R.id.mood_selector_add_btn);
        mMoodValue = root.findViewById(R.id.mood_selector_mood_value);
        
        mLifecycleOwner = lifecycleOwner;
        mFragmentManager = fragmentManager;
        mContext = context;
        
        mMoodViewFactory = createMoodViewFactory();
        
        mViewModel = viewModel;
        mLastSelectedIndex = mViewModel.getMoodIndex();
        bindViewModel();
        
        mAddButton.setOnClickListener(v -> displayMoodDialog(R.string.cancel, createOnCancelListener()));
        
        mMoodValue.setOnClickListener(v -> displayMoodDialog(R.string.delete, createOnDeleteListener()));
    }

//*********************************************************
// api
//*********************************************************

    public void setCallbacks(Callbacks callbacks)
    {
        mCallbacks = callbacks;
    }

//*********************************************************
// private methods
//*********************************************************

    private void displayMoodDialog(
            int negativeTextId,
            MoodDialogFragment.OnClickListener negativeListener)
    {
        MoodDialogFragment.Builder builder = new MoodDialogFragment.Builder(
                mViewModel.getAllMoods(),
                mMoodViewFactory);
        MoodDialogFragment moodDialog = builder
                .setPositiveButton(R.string.confirm, createOnConfirmListener())
                .setNegativeButton(negativeTextId, negativeListener)
                .setSelectedMood(mLastSelectedIndex == MoodSelectorViewModel.NO_MOOD ?
                                         MoodDialogFragment.NO_MOOD_SELECTED : mLastSelectedIndex)
                .build();
        moodDialog.show(mFragmentManager, DIALOG_TAG);
    }
    
    private MoodViewFactory createMoodViewFactory()
    {
        return new EmojiMoodViewFactory();
    }
    
    private MoodDialogFragment.OnClickListener createOnCancelListener()
    {
        return selection -> {/* do nothing */};
    }
    
    private MoodDialogFragment.OnClickListener createOnDeleteListener()
    {
        return selection -> {
            mLastSelectedIndex = MoodDialogFragment.NO_MOOD_SELECTED;
            mViewModel.clearMood();
            if (mCallbacks != null) {
                mCallbacks.onMoodDeleted();
            }
        };
    }
    
    private MoodDialogFragment.OnClickListener createOnConfirmListener()
    {
        return selection -> {
            mLastSelectedIndex = selection.index;
            mViewModel.setMood(selection.mood);
            if (mCallbacks != null) {
                mCallbacks.onMoodChanged(selection.mood);
            }
        };
    }
    
    private void bindViewModel()
    {
        mViewModel.getMood().observe(mLifecycleOwner, mood -> {
            if (mood == null) {
                mAddButton.setVisibility(View.VISIBLE);
                mMoodValue.setVisibility(View.GONE);
            } else {
                mAddButton.setVisibility(View.INVISIBLE);
                mMoodValue.setVisibility(View.VISIBLE);
                
                mMoodValue.removeAllViews();
                mMoodValue.addView(mMoodViewFactory.createView(mood, mContext, 50f));
            }
        });
    }
}
