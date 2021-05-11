package com.rbraithwaite.sleepapp.ui.session_data;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorController;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleepapp.ui.session_data.controllers.DateTimeController;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.io.Serializable;
import java.util.GregorianCalendar;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SessionDataFragment
        extends BaseFragment<SessionDataFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private int mPositiveIcon;
    private int mNegativeIcon;
    private ActionListener mPositiveActionListener;
    private ActionListener mNegativeActionListener;
    
    private DateTimeController mStartDateTimeController;
    private DateTimeController mEndDateTimeController;
    
    private EditText mAdditionalComments;
    
    private MoodSelectorController mMoodSelectorController;
    
    private TagSelectorController mTagSelectorController;
    
    private TagSelectorViewModel mTagSelectorViewModel;
    
    private boolean mIsTagSelectorInitialized = false;
    
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionDataFragment";

//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_ICON = -1;

//*********************************************************
// public helpers
//*********************************************************
    
    public static class Args
            implements Serializable
    {
        public static final long serialVersionUID = 20201230L;
        
        public SleepSessionWrapper initialData;
        public ActionListener positiveActionListener;
        public ActionListener negativeActionListener;
        public int positiveIcon = DEFAULT_ICON;
        public int negativeIcon = DEFAULT_ICON;
    }
    
    public static class ArgsBuilder
    {
        private Args mArgs;
        
        public ArgsBuilder(SleepSessionWrapper initialData)
        {
            mArgs = new Args();
            mArgs.initialData = initialData;
        }
        
        public ArgsBuilder setPositiveActionListener(ActionListener positiveActionListener)
        {
            mArgs.positiveActionListener = positiveActionListener;
            return this;
        }
        
        public ArgsBuilder setNegativeActionListener(ActionListener negativeActionListener)
        {
            mArgs.negativeActionListener = negativeActionListener;
            return this;
        }
        
        public ArgsBuilder setPositiveIcon(int positiveIcon)
        {
            mArgs.positiveIcon = positiveIcon;
            return this;
        }
        
        public ArgsBuilder setNegativeIcon(int negativeIcon)
        {
            mArgs.negativeIcon = negativeIcon;
            return this;
        }
        
        public Args build() {return mArgs;}
    }
    
    public static abstract class ActionListener
            implements Serializable
    {
        public static final long serialVersionUID = 20201230L;
        
        /**
         * The fragment is passed so that clients can control whether or not the fragment is
         * completed, among other things.
         */
        public abstract void onAction(SessionDataFragment fragment, SleepSessionWrapper result);
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SessionDataFragment() { setHasOptionsMenu(true); }
    
//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.session_data_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        SessionDataFragmentArgs safeArgs = SessionDataFragmentArgs.fromBundle(getArguments());
        Args args = safeArgs.getArgs();
        // init members
        mPositiveIcon = args.positiveIcon;
        mNegativeIcon = args.negativeIcon;
        mPositiveActionListener = args.positiveActionListener;
        mNegativeActionListener = args.negativeActionListener;
        
        // init view model
        getViewModel().setSessionData(args.initialData);
        
        // init views
        // REFACTOR [21-03-31 2:13AM] -- make initStartDateTime and initEndDateTime take the
        //  view only, to make things consistent.
        initStartDateTime(view.findViewById(R.id.session_data_start_time));
        initEndDateTime(view.findViewById(R.id.session_data_end_time));
        initSessionDuration(view);
        initAdditionalComments(view);
        initMoodSelector(view);
        initTagSelector(view);
        
        // init back press behaviour
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true)
                {
                    @Override
                    public void handleOnBackPressed()
                    {
                        clearSessionDataThenNavigateUp();
                    }
                });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.session_data_menu, menu);
        if (mPositiveIcon != DEFAULT_ICON) {
            menu.findItem(R.id.session_data_action_positive).setIcon(mPositiveIcon);
        }
        if (mNegativeIcon != DEFAULT_ICON) {
            menu.findItem(R.id.session_data_action_negative).setIcon(mNegativeIcon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // The viewmodel session is cleared here to distinguish cases where the fragment is
        // destroyed on a user action vs by the system (eg an orientation change, where the session
        // should be preserved and the UI re-initialized)
        switch (item.getItemId()) {
        case android.R.id.home: // up button
            clearSessionDataThenNavigateUp();
            return true;
        case R.id.session_data_action_negative:
            if (mNegativeActionListener != null) {
                mNegativeActionListener.onAction(this, getViewModel().getResult());
            } else {
                clearSessionDataThenNavigateUp();
            }
            return true;
        case R.id.session_data_action_positive:
            // REFACTOR [20-12-16 5:56PM] -- should getResult be returning
            //  LiveData<SleepSessionData>? should the implementation be a transformation?
            //  leaving this for now since things seem to be working.
            if (mPositiveActionListener != null) {
                mPositiveActionListener.onAction(this, getViewModel().getResult());
            } else {
                clearSessionDataThenNavigateUp();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return false; }

    @Override
    protected Class<SessionDataFragmentViewModel> getViewModelClass() { return SessionDataFragmentViewModel.class; }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(Args args)
    {
        // use SafeArgs action so that the Bundle works when it is eventually used with
        // SessionDataFragmentArgs.fromBundle()
        // REFACTOR [20-11-28 10:30PM] -- SafeArgs uses the argument names defined in the
        //  navgraph as the Bundle keys - consider redefining those keys here and just making my
        //  own Bundle? problem: the argument names would be hardcoded though, I can't seem to find
        //  a way to get a reference to the names defined in the navgraph, but I should
        //  investigate more.
        return SessionArchiveFragmentDirections
                .actionSessionArchiveToSessionData(args)
                .getArguments();
    }
    
    // TODO [21-12-31 1:54AM] -- I should think more about possible ways of unit testing this.
    public void completed()
    {
        clearSessionDataThenNavigateUp();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initTagSelector(View fragmentRoot)
    {
        mTagSelectorViewModel = new TagSelectorViewModel(requireContext());
        mTagSelectorViewModel.setSelectedTagIds(getViewModel().getTagIds());
        
        mTagSelectorViewModel.getSelectedTags().observe(
                getViewLifecycleOwner(),
                selectedTags -> {
                    // HACK [21-04-22 1:03AM] -- This bool isn't a great solution - I needed
                    //  something to prevent the setSelectedIds() call above from immediately
                    //  notifying this observer with unchanged data.
                    if (!mIsTagSelectorInitialized) {
                        mIsTagSelectorInitialized = true;
                    } else {
                        getViewModel().setTags(selectedTags);
                    }
                });
        
        mTagSelectorController = new TagSelectorController(
                fragmentRoot.findViewById(R.id.session_data_tags),
                mTagSelectorViewModel,
                getViewLifecycleOwner(),
                requireContext(),
                getChildFragmentManager());
    }
    
    private void initMoodSelector(View fragmentRoot)
    {
        mMoodSelectorController = new MoodSelectorController(
                fragmentRoot.findViewById(R.id.session_data_mood),
                // Set the mood selector to the initial mood of the displayed session.
                // There isn't a need to observe this value, as the mood selector will
                // handle its own UI updates.
                new MoodSelectorViewModel(getViewModel().getMood()),
                requireContext(),
                getViewLifecycleOwner(),
                getChildFragmentManager());
        
        mMoodSelectorController.setCallbacks(new MoodSelectorController.Callbacks()
        {
            @Override
            public void onMoodChanged(MoodUiData newMood)
            {
                getViewModel().setMood(newMood);
            }
            
            @Override
            public void onMoodDeleted()
            {
                getViewModel().clearMood();
            }
        });
    }
    
    private void clearSessionDataThenNavigateUp()
    {
        getViewModel().clearSessionData();
        getNavController().navigateUp();
    }
    
    private void initAdditionalComments(View fragmentRoot)
    {
        mAdditionalComments = fragmentRoot.findViewById(R.id.session_data_comments);
        getViewModel().getAdditionalComments().observe(
                getViewLifecycleOwner(),
                s -> {
                    mAdditionalComments.getText().clear();
                    if (s != null) {
                        mAdditionalComments.getText().append(s);
                    }
                });
        mAdditionalComments.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setAdditionalComments(s.toString());
            }
        });
    }
    
    private void initSessionDuration(View fragmentRoot)
    {
        final TextView sessionDurationText = fragmentRoot.findViewById(R.id.session_data_duration);
        getViewModel().getSessionDurationText().observe(
                getViewLifecycleOwner(),
                newSessionDurationText -> {
                    if (newSessionDurationText == null) {
                        sessionDurationText.setText("");
                    } else {
                        sessionDurationText.setText(newSessionDurationText);
                    }
                });
    }
    
    private DateTimeController.Formatter createDateTimeFormatter()
    {
        return new DateTimeController.Formatter()
        {
            @Override
            public String formatTimeOfDay(int hourOfDay, int minute)
            {
                return SessionDataFormatting.formatTimeOfDay(hourOfDay, minute);
            }
            
            @Override
            public String formatDate(int year, int month, int dayOfMonth)
            {
                return SessionDataFormatting.formatDate(year, month, dayOfMonth);
            }
        };
    }
    
    private void initStartDateTime(final View startTimeLayout)
    {
        LiveDataFuture.getValue(
                getViewModel().getStartCalendar(),
                getViewLifecycleOwner(),
                calendar -> {
                    mStartDateTimeController = createDateTimeController(
                            R.string.session_data_start_time_name, calendar, startTimeLayout);
                    
                    mStartDateTimeController.setCallbacks(new DateTimeController.Callbacks()
                    {
                        @Override
                        public boolean beforeSetDate(int year, int month, int dayOfMonth)
                        {
                            try {
                                getViewModel().setStartDate(year, month, dayOfMonth);
                                return true;
                            } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorSnackbar(R.string.error_session_edit_start_datetime);
                                return false;
                            }
                        }
                        
                        @Override
                        public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
                        {
                            try {
                                getViewModel().setStartTimeOfDay(hourOfDay, minute);
                                return true;
                            } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorSnackbar(R.string.error_session_edit_start_datetime);
                                return false;
                            }
                        }
                    });
                });
    }
    
    private void initEndDateTime(final View endTimeLayout)
    {
        LiveDataFuture.getValue(
                getViewModel().getEndCalendar(),
                getViewLifecycleOwner(),
                calendar -> {
                    mEndDateTimeController = createDateTimeController(
                            R.string.session_data_end_time_name, calendar, endTimeLayout);
                    
                    mEndDateTimeController.setCallbacks(new DateTimeController.Callbacks()
                    {
                        @Override
                        public boolean beforeSetDate(int year, int month, int dayOfMonth)
                        {
                            try {
                                getViewModel().setEndDate(year, month, dayOfMonth);
                                return true;
                            } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorSnackbar(R.string.error_session_edit_end_datetime);
                                return false;
                            }
                        }
                        
                        @Override
                        public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
                        {
                            try {
                                getViewModel().setEndTimeOfDay(hourOfDay, minute);
                                return true;
                            } catch (SessionDataFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorSnackbar(R.string.error_session_edit_end_datetime);
                                return false;
                            }
                        }
                    });
                });
    }
    
    private DateTimeController createDateTimeController(
            int titleId,
            GregorianCalendar initialData,
            View root)
    {
        return new DateTimeController(
                getString(titleId),
                initialData,
                root,
                createDateTimeFormatter(),
                getViewLifecycleOwner(),
                getChildFragmentManager());
    }
    
    private void displayErrorSnackbar(int messageId)
    {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }
}
