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
import com.rbraithwaite.sleepapp.ui.common.views.session_times.SessionTimesComponent;
import com.rbraithwaite.sleepapp.ui.common.views.session_times.SessionTimesViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsData;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.ui.utils.AppColors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SessionDetailsFragment
        extends DetailsFragment<SleepSessionWrapper, SessionDetailsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private RatingBar mRatingBar;
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
    
    private static final String DIALOG_OVERLAP_ERROR = "SessionDetailsFragmentOverlapErrorDialog";

//*********************************************************
// public helpers
//*********************************************************

    public static class Result
            extends DetailsResult<SleepSessionWrapper> {}
    
    public static class Args
            extends DetailsFragment.Args<SleepSessionWrapper> {}

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        handleInterruptionDetailsResult(getInterruptionDetailsResult().consumeResult());
        
        initSessionTimes(view);
        initAdditionalComments(view);
        initMoodSelector(view);
        initTagSelector(view);
        initRating(view);
        initInterruptions(view);
    }
    
    @Override
    protected void onAdd()
    {
        try {
            if (getViewModel().checkResultForSessionOverlap()) {
                super.onAdd();
            }
        } catch (SessionDetailsFragmentViewModel.OverlappingSessionException e) {
            displayOverlapErrorDialog(e);
        }
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
    
    @Override
    protected DeleteDialogParams getDeleteDialogParams()
    {
        DeleteDialogParams params = new DeleteDialogParams();
        params.titleId = R.string.session_archive_delete_dialog_title;
        params.messageId = R.string.permanent_operation_message;
        return params;
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
// private methods
//*********************************************************

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
        View dialogContent =
                getLayoutInflater().inflate(R.layout.session_details_overlap_error, null);
        
        TextView start = dialogContent.findViewById(R.id.session_details_overlap_start_value);
        start.setText(e.start);
        
        TextView end = dialogContent.findViewById(R.id.session_details_overlap_end_value);
        end.setText(e.end);
        
        return dialogContent;
    }
    
    private InterruptionDetailsFragment.Result getInterruptionDetailsResult()
    {
        return new ViewModelProvider(requireActivity()).get(InterruptionDetailsFragment.Result.class);
    }
    
    private void handleInterruptionDetailsResult(DetailsResult.Result<InterruptionDetailsData> result)
    {
        if (result == null) {
            // we are not returning from the interruption details fragment, so do nothing
            return;
        }
        
        switch (result.action) {
        case DELETED:
            getViewModel().deleteInterruption(result.data);
            break;
        case UPDATED:
            getViewModel().updateInterruption(result.data);
            break;
        case ADDED:
            getViewModel().addInterruption(result.data);
            break;
        }
    }
    
    private void initSessionTimes(View fragmentRoot)
    {
        // REFACTOR [21-07-29 10:00PM] -- this duplicates InterruptionDetailsFragment.onViewCreated
        //  do I need to do anything about that?
        SessionTimesComponent sessionTimes =
                fragmentRoot.findViewById(R.id.session_details_datetime_content);
        SessionTimesViewModel sessionTimesViewModel = getViewModel().getSessionTimesViewModel();
        sessionTimes.init(this, sessionTimesViewModel);
        
        sessionTimesViewModel.getStart()
                .observe(getViewLifecycleOwner(), start -> getViewModel().setStart(start));
        sessionTimesViewModel.getEnd()
                .observe(getViewLifecycleOwner(), end -> getViewModel().setEnd(end));
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
        
        adapter.setOnAddButtonClickListener(this::navigateToAddInterruptionScreen);
        
        recycler.setAdapter(adapter);
    }
    
    private void navigateToAddInterruptionScreen()
    {
        InterruptionDetailsFragment.Args args = new InterruptionDetailsFragment.Args();
        args.mode = Mode.ADD;
        args.initialData = getViewModel().getNewInterruptionDetailsData();
        
        SessionDetailsFragmentDirections.ActionSessionDetailsToInterruptionDetails
                toAddInterruptionScreen =
                SessionDetailsFragmentDirections.actionSessionDetailsToInterruptionDetails(args);
        
        getNavController().navigate(toAddInterruptionScreen);
    }
    
    private void navigateToEditInterruptionScreen(int interruptionId)
    {
        getNavController().navigate(toEditScreenFor(getViewModel().getInterruptionDetailsData(
                interruptionId)));
    }
    
    private SessionDetailsFragmentDirections.ActionSessionDetailsToInterruptionDetails toEditScreenFor(
            InterruptionDetailsData data)
    {
        InterruptionDetailsFragment.Args args = new InterruptionDetailsFragment.Args();
        args.mode = Mode.UPDATE;
        args.initialData = data;
        
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
                    // REFACTOR [21-07-29 8:00PM] -- getAdditionalComments should never return null,
                    //  so this can just be like the reason text in InterruptionDetailsFragment
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
}
