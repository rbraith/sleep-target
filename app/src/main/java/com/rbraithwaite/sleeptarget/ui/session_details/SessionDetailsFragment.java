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
package com.rbraithwaite.sleeptarget.ui.session_details;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.SessionDetailsFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment2;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsResult;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorController;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.session_times.SessionTimesViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.interruption_details.InterruptionDetailsFragment;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.AfterTextChangedWatcher;
import com.rbraithwaite.sleeptarget.ui.utils.AppColors;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;

import dagger.hilt.android.AndroidEntryPoint;

// SMELL [21-09-10 5:30PM] -- It's weird that when adding a new session this uses
//  SleepSession instead of SleepSessionRepository.NewSleepSessionData (as is the case when
//  adding a session from the sleep tracker screen) - is there anything I should do about this?
//  Note that in SessionArchiveFragmentViewModel.addSleepSession() the SleepSession is converted
//  to a NewSleepSessionData for the repo.
@AndroidEntryPoint
public class SessionDetailsFragment
        extends DetailsFragment<SleepSessionWrapper, SessionDetailsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private SessionDetailsFragmentBinding mBinding;
    
    private MoodSelectorController mMoodSelectorController; // no gc
    
    private TagSelectorViewModel mTagSelectorViewModel;
    private MoodSelectorViewModel mMoodSelectorViewModel;
    
    private boolean mIsTagSelectorInitialized = false;
    
    private SessionTimesViewModel mSessionTimesViewModel;

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
    
    public static class OverlapErrorDialog
            extends AlertDialogFragment2
    {
        public OverlapErrorDialog() {}
        
        public OverlapErrorDialog(SessionDetailsFragmentViewModel.OverlappingSessionException e)
        {
            Bundle args = new Bundle();
            args.putString("start", e.start);
            args.putString("end", e.end);
            setArguments(args);
        }
        
        @Override
        protected AlertDialog createAlertDialog()
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Error: Overlapping Sleep Session")
                    .setView(createOverlapErrorDialogContent())
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }
        
        private View createOverlapErrorDialogContent()
        {
            @SuppressLint("InflateParams") View dialogContent =
                    getLayoutInflater().inflate(R.layout.session_details_overlap_error, null);
            
            TextView start = dialogContent.findViewById(R.id.session_details_overlap_start_value);
            start.setText(getArguments().getString("start"));
            
            TextView end = dialogContent.findViewById(R.id.session_details_overlap_end_value);
            end.setText(getArguments().getString("end"));
            
            return dialogContent;
        }
    }

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
        mBinding = SessionDetailsFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
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
        
        getViewModel().handleInterruptionDetailsResult(getInterruptionDetailsResult());
        
        initSessionTimes();
        initAdditionalComments();
        initMoodSelector();
        initTagSelector();
        initRating();
        initInterruptions();
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
        return mBinding.ratingBar;
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
        new OverlapErrorDialog(e).show(getChildFragmentManager(), DIALOG_OVERLAP_ERROR);
    }
    
    private InterruptionDetailsFragment.Result getInterruptionDetailsResult()
    {
        return new ViewModelProvider(requireActivity()).get(InterruptionDetailsFragment.Result.class);
    }
    
    private SessionTimesViewModel getSessionTimesViewModel()
    {
        mSessionTimesViewModel = CommonUtils.lazyInit(mSessionTimesViewModel, () -> {
            SessionTimesViewModel sessionTimesViewModel =
                    new ViewModelProvider(this).get(SessionTimesViewModel.class);
            sessionTimesViewModel.init(getViewModel().getSession());
            return sessionTimesViewModel;
        });
        return mSessionTimesViewModel;
    }
    
    private void initSessionTimes()
    {
        SessionTimesViewModel sessionTimesViewModel = getSessionTimesViewModel();
        mBinding.datetimeContent.init(this, sessionTimesViewModel);
        
        sessionTimesViewModel.getStart()
                .observe(getViewLifecycleOwner(), start -> getViewModel().setStart(start));
        sessionTimesViewModel.getEnd()
                .observe(getViewLifecycleOwner(), end -> getViewModel().setEnd(end));
    }
    
    private void initInterruptions()
    {
        SessionDetailsFragmentViewModel viewModel = getViewModel();
        
        // totals
        // ------------------------------------------------------
        mBinding.interruptionsContent.interruptionsCount.setText(viewModel.getInterruptionsCountText());
        mBinding.interruptionsContent.interruptionsTotal.setText(viewModel.getInterruptionsTotalTimeText());
        if (viewModel.hasNoInterruptions()) {
            // grey out the totals
            AppColors appColors = AppColors.from(requireContext());
            mBinding.interruptionsContent.interruptionsCount.setTextColor(appColors.appColorOnPrimarySurface2);
            mBinding.interruptionsContent.interruptionsTotal.setTextColor(appColors.appColorOnPrimarySurface2);
        }
        
        // recycler
        // ------------------------------------------------------
        mBinding.interruptionsContent.interruptionsRecycler.setLayoutManager(new LinearLayoutManager(
                requireContext()));
        
        SessionDetailsInterruptionsAdapter adapter = new SessionDetailsInterruptionsAdapter(
                viewHolder -> navigateToEditInterruptionScreenFor(viewHolder.data.interruptionId));
        
        adapter.setOnAddButtonClickListener(this::navigateToAddInterruptionScreen);
        
        getViewModel().getInterruptionListItems().observe(getViewLifecycleOwner(),
                                                          adapter::setItems);
        
        mBinding.interruptionsContent.interruptionsRecycler.setAdapter(adapter);
    }
    
    private void navigateToAddInterruptionScreen()
    {
        SessionDetailsFragmentDirections.ActionSessionDetailsToInterruptionDetails
                toAddInterruptionScreen =
                SessionDetailsFragmentDirections.actionSessionDetailsToInterruptionDetails(
                        getViewModel().getInterruptionDetailsArgsForAdd());
        
        getNavController().navigate(toAddInterruptionScreen);
    }
    
    private void navigateToEditInterruptionScreenFor(int interruptionId)
    {
        SessionDetailsFragmentDirections.ActionSessionDetailsToInterruptionDetails
                toEditInterruptionScreen =
                SessionDetailsFragmentDirections.actionSessionDetailsToInterruptionDetails(
                        getViewModel().getInterruptionDetailsEditArgsFor(interruptionId));
        
        getNavController().navigate(toEditInterruptionScreen);
    }
    
    private void initRating()
    {
        mBinding.ratingBar.setRating(getViewModel().getRating());
        mBinding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> getViewModel()
                .setRating(
                        rating));
    }
    
    private void initTagSelector()
    {
        mTagSelectorViewModel = TagSelectorViewModel.getInstanceFrom(requireActivity());
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
        
        mBinding.tags.init(
                mTagSelectorViewModel,
                getViewLifecycleOwner(),
                getChildFragmentManager());
    }
    
    private void initMoodSelector()
    {
        mMoodSelectorViewModel = MoodSelectorViewModel.getInstanceFrom(requireActivity());
        mMoodSelectorViewModel.setMood(getViewModel().getMood());
        mMoodSelectorViewModel.getMood().observe(getViewLifecycleOwner(), getViewModel()::setMood);
        
        mMoodSelectorController = new MoodSelectorController(
                mBinding.mood,
                // Set the mood selector to the initial mood of the displayed session.
                // There isn't a need to observe this value, as the mood selector will
                // handle its own UI updates.
                mMoodSelectorViewModel,
                getViewLifecycleOwner(),
                getChildFragmentManager());
    }
    
    private void initAdditionalComments()
    {
        getViewModel().getAdditionalComments().observe(
                getViewLifecycleOwner(),
                s -> {
                    UiUtils.setEditTextValue(mBinding.comments, s);
                });
        
        mBinding.comments.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setAdditionalComments(s.toString());
            }
        });
    }
}
