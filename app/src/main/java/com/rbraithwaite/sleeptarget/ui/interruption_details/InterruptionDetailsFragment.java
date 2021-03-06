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
package com.rbraithwaite.sleeptarget.ui.interruption_details;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.InterruptionDetailsErrorBinding;
import com.rbraithwaite.sleeptarget.databinding.InterruptionDetailsFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment2;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsResult;
import com.rbraithwaite.sleeptarget.ui.common.views.session_times.SessionTimesComponent;
import com.rbraithwaite.sleeptarget.ui.common.views.session_times.SessionTimesViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.AfterTextChangedWatcher;
import com.rbraithwaite.sleeptarget.ui.utils.UiUtils;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InterruptionDetailsFragment
        extends DetailsFragment<InterruptionDetailsData, InterruptionDetailsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private InterruptionDetailsFragmentBinding mBinding;
    
    private SessionTimesViewModel mSessionTimesViewModel;
    private ConstraintLayout mOutOfBoundsWarning;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_OVERLAP_ERROR =
            "InterruptionDetailsFragmentOverlapErrorDialog";
    
    private static final String DIALOG_OUT_OF_BOUNDS_ERROR =
            "InterruptionDetailsFragmentOOBErrorDialog";

//*********************************************************
// public helpers
//*********************************************************

    public static class Result
            extends DetailsResult<InterruptionDetailsData> {}
    
    public static class Args
            extends DetailsFragment.Args<InterruptionDetailsData> {}

    public static class OverlapErrorDialog
            extends AlertDialogFragment2
    {
        public OverlapErrorDialog() {}
        
        public OverlapErrorDialog(InterruptionDetailsFragmentViewModel.OverlappingInterruptionException e)
        {
            Bundle args = new Bundle();
            args.putString("overlap start", e.overlappedStart);
            args.putString("overlap end", e.overlappedEnd);
            setArguments(args);
        }
        
        @Override
        protected AlertDialog createAlertDialog()
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.interruption_details_overlap_error_title)
                    .setView(createDialogContent())
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }
        
        private View createDialogContent()
        {
            @SuppressLint("InflateParams") View dialogContent =
                    getLayoutInflater().inflate(R.layout.interruption_details_error, null);
            InterruptionDetailsErrorBinding binding = InterruptionDetailsErrorBinding.bind(dialogContent);
            
            binding.message.setText(R.string.interruption_details_overlap_error_message);
            
            binding.startValue.setText(getArguments().getString("overlap start"));
            
            binding.endValue.setText(getArguments().getString("overlap end"));
            
            return binding.getRoot();
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
        mBinding = InterruptionDetailsFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    protected DetailsFragment.Args<InterruptionDetailsData> getDetailsArgs()
    {
        InterruptionDetailsFragmentArgs safeArgs =
                InterruptionDetailsFragmentArgs.fromBundle(getArguments());
        return safeArgs.getArgs();
    }
    
    @Override
    protected Class<? extends DetailsResult<InterruptionDetailsData>> getResultClass()
    {
        return InterruptionDetailsFragment.Result.class;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        // init session times
        SessionTimesViewModel sessionTimesViewModel = getSessionTimesViewModel();
        mBinding.sessionTimes.init(this, sessionTimesViewModel);
        
        sessionTimesViewModel.getStart()
                .observe(getViewLifecycleOwner(), start -> getViewModel().setStart(start));
        sessionTimesViewModel.getEnd()
                .observe(getViewLifecycleOwner(), end -> getViewModel().setEnd(end));
        
        // init reason
        getViewModel().getReason()
                .observe(getViewLifecycleOwner(),
                         reason -> UiUtils.setEditTextValue(mBinding.reason, reason));
        mBinding.reason.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setReason(s.toString());
            }
        });
        
        // init out-of-bounds warning
        getViewModel().isOutOfBounds().observe(getViewLifecycleOwner(), outOfBoundsDetails -> {
            if (outOfBoundsDetails != null) {
                displayOutOfBoundsWarning(outOfBoundsDetails);
            } else {
                hideOutOfBoundsWarning();
            }
        });
    }
    
    @Override
    protected void onAdd()
    {
        if (checkForValidResult()) {
            super.onAdd();
        }
    }
    
    @Override
    protected void onUpdate()
    {
        if (checkForValidResult()) {
            super.onUpdate();
        }
    }
    
    @Override
    protected DeleteDialogParams getDeleteDialogParams()
    {
        DeleteDialogParams params = new DeleteDialogParams();
        params.titleId = R.string.interruption_details_delete_title;
        params.messageId = R.string.permanent_operation_message;
        return params;
    }

    @Override
    protected Properties<InterruptionDetailsFragmentViewModel> initProperties()
    {
        return new Properties<>(false, InterruptionDetailsFragmentViewModel.class);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void hideOutOfBoundsWarning()
    {
        mBinding.boundsWarning.getRoot().setVisibility(View.GONE);
    }
    
    private void displayOutOfBoundsWarning(InterruptionDetailsFragmentViewModel.OutOfBoundsDetails outOfBoundsDetails)
    {
        mBinding.boundsWarning.getRoot().setVisibility(View.VISIBLE);
        mBinding.boundsWarning.message.setText(outOfBoundsDetails.messageId);
        mBinding.boundsWarning.time.setText(outOfBoundsDetails.sessionTimeText);
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
    
    /**
     * If the result interruption is not valid, display an error dialog and return false.
     */
    private boolean checkForValidResult()
    {
        try {
            getViewModel().checkForValidResult();
            return true;
        } catch (InterruptionDetailsFragmentViewModel.OverlappingInterruptionException e) {
            displayOverlapErrorDialog(e);
            return false;
        }
    }
    
    private void displayOverlapErrorDialog(InterruptionDetailsFragmentViewModel.OverlappingInterruptionException e)
    {
        new OverlapErrorDialog(e).show(getChildFragmentManager(), DIALOG_OVERLAP_ERROR);
    }
}
