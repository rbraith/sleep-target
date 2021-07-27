package com.rbraithwaite.sleepapp.ui.session_details;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsResult;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionWrapper;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleepapp.ui.session_details.controllers.DateTimeController;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.ui.utils.AppColors;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.util.GregorianCalendar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionDetailsFragment
        extends DetailsFragment<SleepSessionWrapper, SessionDetailsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************
    
    private RatingBar mRatingBar;
    
    private DateTimeController mStartDateTimeController;
    private DateTimeController mEndDateTimeController;
    
    private EditText mAdditionalComments;
    
    private MoodSelectorController mMoodSelectorController;
    
    private TagSelectorController mTagSelectorController;
    
    private TagSelectorViewModel mTagSelectorViewModel;
    private MoodSelectorViewModel mMoodSelectorViewModel;
    
    private boolean mIsTagSelectorInitialized = false;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionDetailsFragment";
    
    private static final String DIALOG_ERROR = "DialogError";

//*********************************************************
// public helpers
//*********************************************************
    
    public static class Result extends DetailsResult<SleepSessionWrapper> {}
    public static class Args extends DetailsFragment.Args<SleepSessionWrapper> {}
    
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
        return inflater.inflate(R.layout.session_details_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        handleInterruptionDetailsResult(getInterruptionDetailsResult().consumeResult());
        
        // REFACTOR [21-03-31 2:13AM] -- make initStartDateTime and initEndDateTime take the
        //  view only, to make things consistent.
        initStartDateTime(view.findViewById(R.id.session_details_start_time));
        initEndDateTime(view.findViewById(R.id.session_details_end_time));
        initSessionDuration(view);
        initAdditionalComments(view);
        initMoodSelector(view);
        initTagSelector(view);
        initRating(view);
        initInterruptions(view);
    }
    
    @Override
    protected Properties<SessionDetailsFragmentViewModel> initProperties()
    {
        return new Properties<>(false, SessionDetailsFragmentViewModel.class);
    }
    
    @Override
    protected DetailsFragment.Args<SleepSessionWrapper> getDetailsArgs()
    {
        SessionDetailsFragmentArgs safeArgs = SessionDetailsFragmentArgs.fromBundle(getArguments());
        return safeArgs.getArgs();
    }
    
    @Override
    protected Class<? extends DetailsResult<SleepSessionWrapper>> getResultClass()
    {
        return SessionDetailsFragment.Result.class;
    }
    
    @Override
    protected DeleteDialogParams getDeleteDialogParams()
    {
        DeleteDialogParams params = new DeleteDialogParams();
        params.titleId = R.string.session_archive_delete_dialog_title;
        params.messageId = R.string.permanent_operation_message;
        return params;
    }
    
    @Override
    protected void onUpdate()
    {
        try {
            if (getViewModel().checkResultForSessionOverlap()) {
                super.onUpdate();
            }
        } catch (SessionDetailsFragmentViewModel.OverlappingSessionException e) {
            displayOverlapErrorDialog(e);
        }
    }
    
    private static final String DIALOG_OVERLAP_ERROR = "SessionDetailsFragmentOverlapErrorDialog";
    
    private void displayOverlapErrorDialog(SessionDetailsFragmentViewModel.OverlappingSessionException e)
    {
        AlertDialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Error: Overlapping Sleep Session")
                    .setView(createOverlapErrorDialogContent(e))
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        
        dialog.show(getChildFragmentManager(), DIALOG_OVERLAP_ERROR);
    }
    
    private View createOverlapErrorDialogContent(SessionDetailsFragmentViewModel.OverlappingSessionException e)
    {
        View dialogContent = getLayoutInflater().inflate(R.layout.session_details_overlap_error, null);
        
        TextView start = dialogContent.findViewById(R.id.session_details_overlap_start_value);
        start.setText(e.start);
        
        TextView end = dialogContent.findViewById(R.id.session_details_overlap_end_value);
        end.setText(e.end);
        
        return dialogContent;
    }
    
    public RatingBar getRatingBar()
    {
        return mRatingBar;
    }
    
    public TagSelectorViewModel getTagSelectorViewModel()
    {
        return mTagSelectorViewModel;
    }
    
    public MoodSelectorViewModel getMoodSelectorViewModel()
    {
        return mMoodSelectorViewModel;
    }

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

//*********************************************************
// private methods
//*********************************************************
    
    private InterruptionDetailsFragment.Result getInterruptionDetailsResult()
    {
        return new ViewModelProvider(requireActivity()).get(InterruptionDetailsFragment.Result.class);
    }
    
    private void handleInterruptionDetailsResult(DetailsResult.Result<InterruptionWrapper> result)
    {
        if (result == null) {
            // we are not returning from the interruption details fragment, so do nothing
            return;
        }
        
        switch (result.action) {
        case DELETED:
            getViewModel().deleteInterruption(result.data);
            // TODO [21-07-23 5:38PM] -- remaining cases.
        }
    }

    private void initInterruptions(View fragmentRoot)
    {
        SessionDetailsFragmentViewModel viewModel = getViewModel();
        
        View card = fragmentRoot.findViewById(R.id.session_details_interruptions_card);
        
        // totals
        // ------------------------------------------------------
        TextView interruptionsTotalCount = card.findViewById(R.id.common_interruptions_count);
        TextView interruptionsTotalTime = card.findViewById(R.id.common_interruptions_total);
        
        interruptionsTotalCount.setText(viewModel.getInterruptionsCountText());
        interruptionsTotalTime.setText(viewModel.getInterruptionsTotalTimeText());
        if (viewModel.hasNoInterruptions()) {
            // grey out the totals
            AppColors appColors = AppColors.from(requireContext());
            interruptionsTotalCount.setTextColor(appColors.appColorOnPrimarySurface2);
            interruptionsTotalTime.setTextColor(appColors.appColorOnPrimarySurface2);
        }
        
        // recycler
        // ------------------------------------------------------
        RecyclerView recycler = fragmentRoot.findViewById(R.id.common_interruptions_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        SessionDetailsInterruptionsAdapter adapter = new SessionDetailsInterruptionsAdapter(
                getViewModel().getInterruptionListItems(),
                viewHolder -> navigateToEditInterruptionScreen(viewHolder.data.interruptionId));
        
        recycler.setAdapter(adapter);
    }
    
    private void navigateToEditInterruptionScreen(int interruptionId)
    {
        getNavController().navigate(toEditScreenFor(getViewModel().getInterruption(interruptionId)));
    }
    
    private SessionDetailsFragmentDirections.ActionSessionDetailsToInterruptionDetails toEditScreenFor(
            InterruptionWrapper interruption)
    {
        InterruptionDetailsFragment.Args args = new InterruptionDetailsFragment.Args();
        args.mode = Mode.UPDATE;
        args.initialData = interruption;
        
        return SessionDetailsFragmentDirections.actionSessionDetailsToInterruptionDetails(args);
    }
    
    private void initRating(View fragmentRoot)
    {
        mRatingBar = fragmentRoot.findViewById(R.id.session_details_rating);
        mRatingBar.setRating(getViewModel().getRating());
        
        mRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> getViewModel().setRating(
                rating));
    }
    
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
                fragmentRoot.findViewById(R.id.session_details_tags),
                mTagSelectorViewModel,
                getViewLifecycleOwner(),
                requireContext(),
                getChildFragmentManager());
    }
    
    private void initMoodSelector(View fragmentRoot)
    {
        mMoodSelectorViewModel = new MoodSelectorViewModel(getViewModel().getMood());
        
        mMoodSelectorController = new MoodSelectorController(
                fragmentRoot.findViewById(R.id.session_details_mood),
                // Set the mood selector to the initial mood of the displayed session.
                // There isn't a need to observe this value, as the mood selector will
                // handle its own UI updates.
                mMoodSelectorViewModel,
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
    
    private void initAdditionalComments(View fragmentRoot)
    {
        mAdditionalComments = fragmentRoot.findViewById(R.id.session_details_comments);
        getViewModel().getAdditionalComments().observe(
                getViewLifecycleOwner(),
                s -> {
                    mAdditionalComments.getText().clear();
                    if (s != null) {
                        mAdditionalComments.getText().append(s);
                    }
                });
        // REFACTOR [21-07-22 12:51AM] -- this can be an AfterTextChangedWatcher.
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
        final TextView sessionDurationText =
                fragmentRoot.findViewById(R.id.session_details_duration);
        getViewModel().getSessionDurationText().observe(
                getViewLifecycleOwner(),
                newSessionDurationText -> {
                    // REFACTOR [21-06-16 10:31PM] this setting to "" logic should just be
                    //  in the view model, and this value shouldn't be nullable.
                    if (newSessionDurationText == null) {
                        sessionDurationText.setText("");
                    } else {
                        sessionDurationText.setText(newSessionDurationText);
                    }
                });
    }
    
    // TODO [21-06-16 10:33PM] does it make sense to parameterize the formatting of
    //  the datetime views like this?
    private DateTimeController.Formatter createDateTimeFormatter()
    {
        return new DateTimeController.Formatter()
        {
            @Override
            public String formatTimeOfDay(int hourOfDay, int minute)
            {
                return SessionDetailsFormatting.formatTimeOfDay(hourOfDay, minute);
            }
            
            @Override
            public String formatDate(int year, int month, int dayOfMonth)
            {
                return SessionDetailsFormatting.formatDate(year, month, dayOfMonth);
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
                            } catch (SessionDetailsFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorDialog(R.string.error_session_edit_start_datetime);
                                return false;
                            } catch (SessionDetailsFragmentViewModel.FutureDateTimeException e) {
                                // TODO [21-07-2 2:22AM] -- preferably the user wouldn't even
                                //  have the option to set future times (this could be tricky
                                //  though,
                                //  due to how the Date & Time of Day are set separately).
                                displayErrorDialog(R.string.session_details_future_time_error);
                                return false;
                            }
                        }
                        
                        @Override
                        public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
                        {
                            // REFACTOR [21-07-1 9:13PM] -- Is there any way I can DRY these
                            //  repeated try/catch blocks?.
                            try {
                                getViewModel().setStartTimeOfDay(hourOfDay, minute);
                                return true;
                            } catch (SessionDetailsFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorDialog(R.string.error_session_edit_start_datetime);
                                return false;
                            } catch (SessionDetailsFragmentViewModel.FutureDateTimeException e) {
                                displayErrorDialog(R.string.session_details_future_time_error);
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
                            } catch (SessionDetailsFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorDialog(R.string.error_session_edit_end_datetime);
                                return false;
                            } catch (SessionDetailsFragmentViewModel.FutureDateTimeException e) {
                                displayErrorDialog(R.string.session_details_future_time_error);
                                return false;
                            }
                        }
                        
                        @Override
                        public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
                        {
                            try {
                                getViewModel().setEndTimeOfDay(hourOfDay, minute);
                                return true;
                            } catch (SessionDetailsFragmentViewModel.InvalidDateTimeException e) {
                                displayErrorDialog(R.string.error_session_edit_end_datetime);
                                return false;
                            } catch (SessionDetailsFragmentViewModel.FutureDateTimeException e) {
                                displayErrorDialog(R.string.session_details_future_time_error);
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
    
    // REFACTOR [21-06-16 10:34PM] this should be extracted somewhere as a common utility.
    private void displayErrorDialog(int messageId)
    {
        AlertDialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage(messageId)
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        dialog.show(getChildFragmentManager(), DIALOG_ERROR);
    }
}
