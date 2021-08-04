package com.rbraithwaite.sleepapp.ui.interruption_details;

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

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsResult;
import com.rbraithwaite.sleepapp.ui.common.views.session_times.SessionTimesComponent;
import com.rbraithwaite.sleepapp.ui.common.views.session_times.SessionTimesViewModel;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.AfterTextChangedWatcher;
import com.rbraithwaite.sleepapp.ui.utils.UiUtils;

public class InterruptionDetailsFragment
        extends DetailsFragment<InterruptionDetailsData, InterruptionDetailsFragmentViewModel>
{
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
        return inflater.inflate(R.layout.interruption_details_fragment, container, false);
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
        SessionTimesComponent sessionTimes =
                view.findViewById(R.id.interruption_details_session_times);
        SessionTimesViewModel sessionTimesViewModel = getViewModel().getSessionTimesViewModel();
        sessionTimes.init(this, sessionTimesViewModel);
        
        sessionTimesViewModel.getStart()
                .observe(getViewLifecycleOwner(), start -> getViewModel().setStart(start));
        sessionTimesViewModel.getEnd()
                .observe(getViewLifecycleOwner(), end -> getViewModel().setEnd(end));
        
        // init reason
        EditText reasonText = view.findViewById(R.id.interruptions_details_reason);
        getViewModel().getReason()
                .observe(getViewLifecycleOwner(),
                         reason -> UiUtils.setEditTextValue(reasonText, reason));
        reasonText.addTextChangedListener(new AfterTextChangedWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                getViewModel().setReason(s.toString());
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
        } catch (InterruptionDetailsFragmentViewModel.OutOfBoundsInterruptionException e) {
            displayOutOfBoundsErrorDialog(e);
            return false;
        }
    }
    
    private void displayErrorDialog(String tag, int titleId, View content)
    {
        AlertDialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(titleId)
                    .setView(content)
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        
        dialog.show(getChildFragmentManager(), tag);
    }
    
    private void displayOutOfBoundsErrorDialog(InterruptionDetailsFragmentViewModel.OutOfBoundsInterruptionException e)
    {
        displayErrorDialog(
                DIALOG_OUT_OF_BOUNDS_ERROR,
                R.string.interruption_details_oob_error_title,
                createOutOfBoundsErrorDialogContent(e));
    }
    
    private void displayOverlapErrorDialog(InterruptionDetailsFragmentViewModel.OverlappingInterruptionException e)
    {
        displayErrorDialog(
                DIALOG_OVERLAP_ERROR,
                R.string.interruption_details_overlap_error_title,
                createOverlapErrorDialogContent(e));
    }
    
    private View createOutOfBoundsErrorDialogContent(InterruptionDetailsFragmentViewModel.OutOfBoundsInterruptionException e)
    {
        return createErrorDialogContent(
                R.string.interruption_details_oob_error_message,
                e.sessionStart,
                e.sessionEnd);
    }
    
    private View createOverlapErrorDialogContent(InterruptionDetailsFragmentViewModel.OverlappingInterruptionException e)
    {
        return createErrorDialogContent(
                R.string.interruption_details_overlap_error_message,
                e.overlappedStart,
                e.overlappedEnd);
    }
    
    private View createErrorDialogContent(
            int messageId,
            String startText,
            String endText)
    {
        View dialogContent = getLayoutInflater().inflate(R.layout.interruption_details_error, null);
        
        TextView message = dialogContent.findViewById(R.id.interruption_details_error_message);
        message.setText(messageId);
        
        TextView start = dialogContent.findViewById(R.id.interruption_details_error_start_value);
        start.setText(startText);
        
        TextView end = dialogContent.findViewById(R.id.interruption_details_error_end_value);
        end.setText(endText);
        
        return dialogContent;
    }
}
